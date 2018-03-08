package com.blackducksoftware.integration.hub.detectws.app;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.test.annotation.IntegrationTest;

import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

@Category(IntegrationTest.class)
public class InMinikubeTest {
    private static final String POD_NAME = "hub-detect-ws";
    private static final String PORT = "8080";
    private static KubernetesClient client;
    private static String clusterIp;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        final String kubeStatusOutputJoined = execCmd("minikube status", 15);
        System.out.println(String.format("kubeStatusOutputJoined: %s", kubeStatusOutputJoined));
        assertTrue("Minikube is not running", kubeStatusOutputJoined.contains("minikube: Running"));
        assertTrue("Minikube is not running", kubeStatusOutputJoined.contains("cluster: Running"));

        final String[] ipOutput = execCmd("minikube ip", 10).split("\n");
        clusterIp = ipOutput[0];
        client = new DefaultKubernetesClient();
        try {
            System.out.printf("API version: %s\n", client.getApiVersion());
        } catch (final Exception e) {
            e.printStackTrace();
        }

        final String[] dockerEnvOutput = execCmd("minikube docker-env", 5).split("\n");
        final Map<String, String> dockerEnv = new HashMap<>();
        for (final String line : dockerEnvOutput) {
            if (line.startsWith("export")) {
                final String envVariableName = line.substring("export".length() + 1, line.indexOf("="));
                final String envVariableValue = line.substring(line.indexOf("=") + 2, line.length() - 1);
                System.out.println(String.format("env var assignment: %s=%s", envVariableName, envVariableValue));
                dockerEnv.put(envVariableName, envVariableValue);
            }
        }

        execCmd("mkdir -p build/test/target", 5);
        execCmd("docker pull alpine:latest", 120, dockerEnv);
        execCmd("docker save -o build/test/target/alpine.tar alpine:latest", 20, dockerEnv);
        execCmd("chmod a+r build/test/target/alpine.tar", 5);

        // execCmd("docker pull debian:latest", 120, dockerEnv);
        // execCmd("docker save -o build/test/target/debian.tar debian:latest", 20, dockerEnv);
        // execCmd("chmod a+r build/test/target/debian.tar", 5);
        //
        // execCmd("docker pull fedora:latest", 120, dockerEnv);
        // execCmd("docker save -o build/test/target/fedora.tar fedora:latest", 20, dockerEnv);
        // execCmd("chmod a+r build/test/target/fedora.tar", 5);

        InputStream configInputStream = InMinikubeTest.class.getResourceAsStream("kube-test-pod.yml");
        if (configInputStream == null) {
            final File configFile = new File("build/classes/java/test/com/blackducksoftware/integration/hub/detectws/app/kube-test-pod.yml");
            assertTrue("Unable to find pod config file", configFile.exists());
            configInputStream = new FileInputStream(configFile);
        }
        assertNotNull("Unable to load pod config file", configInputStream);
        client.load(configInputStream).inNamespace("default").createOrReplace();
        Thread.sleep(10000L);
        client.load(InMinikubeTest.class.getResourceAsStream("/kube-service.yml")).inNamespace("default").createOrReplace();
        Thread.sleep(5000L);

        final PodList podList = client.pods().inNamespace("default").list();
        final String podListString = podList.getItems().stream().map(pod -> pod.getMetadata().getName()).collect(Collectors.joining("\n"));
        System.out.printf("Pods: %s\n", podListString);

        final ServiceList serviceList = client.services().inNamespace("default").list();
        for (final Service service : serviceList.getItems()) {
            System.out.printf("Service: %s; app: %s\n", service.getMetadata().getName(), service.getMetadata().getLabels().get("app"));
        }
        Thread.sleep(20000L);
        assertTrue("never got a successful service health check", isServiceHealthy(PORT));
        System.out.println("The service is ready");

        Thread.sleep(20000L);
    }

    @AfterClass
    public static void tearDownAfterClass() {
        if (client != null) {
            client.close();
        }
        try {
            execCmd(String.format("kubectl delete service %s", POD_NAME), 10);
        } catch (IOException | InterruptedException | IntegrationException e1) {
        }
        try {
            execCmd(String.format("kubectl delete pod %s", POD_NAME), 10);
        } catch (IOException | InterruptedException | IntegrationException e1) {
        }
        try {
            Thread.sleep(20000L);
        } catch (final InterruptedException e1) {
        }
        boolean podExited = false;
        for (int i = 0; i < 10; i++) {
            try {
                execCmd(String.format("kubectl get pod %s", POD_NAME), 10);
            } catch (final Exception e) {
                System.out.println(String.format("kubectl get pod %s failed: %s", POD_NAME, e.getMessage()));
                if (e.getMessage().contains("NotFound")) {
                    podExited = true;
                    break;
                } else {
                    System.out.println("Don't understand this error; continuing to wait...");
                }
            }
            try {
                Thread.sleep(10000L);
            } catch (final InterruptedException e) {
            }
        }
        if (!podExited) {
            System.out.println(String.format("Warning: Pod %s has not exited", POD_NAME));
        }
        System.out.println("Test has completed");
    }

    @Test
    public void test() throws InterruptedException, IntegrationException, IOException {
        final String getBdioOutputJoined = execCmd(String.format("curl -i http://%s:%s/scaninspectimage?tarfile=/opt/blackduck/hub-detect-ws/target/alpine.tar", clusterIp, PORT), 30);
        System.out.printf("getBdioOutputJoined: %s", getBdioOutputJoined);
        assertTrue(getBdioOutputJoined.contains("scan/inspect image operation mocked"));
    }

    private static String execCmd(final String cmd, final long timeout) throws IOException, InterruptedException, IntegrationException {
        return execCmd(cmd, timeout, null);
    }

    private static String execCmd(final String cmd, final long timeout, final Map<String, String> env) throws IOException, InterruptedException, IntegrationException {
        System.out.println(String.format("Executing: %s", cmd));
        final ProcessBuilder pb = new ProcessBuilder("bash", "-c", cmd);
        pb.redirectOutput(Redirect.PIPE);
        pb.redirectError(Redirect.PIPE);
        pb.environment().put("PATH", "/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin");
        if (env != null) {
            pb.environment().putAll(env);
        }
        final Process p = pb.start();
        final String stdoutString = toString(p.getInputStream());
        final String stderrString = toString(p.getErrorStream());
        final boolean finished = p.waitFor(timeout, TimeUnit.SECONDS);
        if (!finished) {
            throw new InterruptedException(String.format("Command '%s' timed out", cmd));
        }

        System.out.println(String.format("%s: stdout: %s", cmd, stdoutString));
        System.out.println(String.format("%s: stderr: %s", cmd, stderrString));
        final int retCode = p.exitValue();
        if (retCode != 0) {
            System.out.println(String.format("%s: retCode: %d", cmd, retCode));
            throw new IntegrationException(String.format("Command '%s' failed: %s", cmd, stderrString));
        }
        return stdoutString;
    }

    private static String toString(final InputStream is) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        final StringBuilder builder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            if (builder.length() > 0) {
                builder.append("\n");
            }
            builder.append(line);
        }
        return builder.toString();
    }

    private static boolean isServiceHealthy(final String port) throws InterruptedException, IOException {
        boolean serviceIsHealthy = false;
        final int healthCheckLimit = 30;
        for (int i = 0; i < healthCheckLimit; i++) {
            String[] healthCheckOutput;
            try {
                System.out.printf("Port %s Health check attempt %d of %d:\n", port, i, healthCheckLimit);
                healthCheckOutput = execCmd(String.format("curl -i http://%s:%s/health", clusterIp, port), 10).split("\n");
                for (final String line : healthCheckOutput) {
                    System.out.printf("Port %s Health check output: %s\n", port, line);
                    if (line.startsWith("HTTP") && line.contains(" 200")) {
                        System.out.printf("Port %s Health check passed\n", port);
                        serviceIsHealthy = true;
                        break;
                    }
                }
                if (serviceIsHealthy) {
                    break;
                }
            } catch (final IntegrationException e) {
                System.out.printf("Port %s Health check failed: %s\n", port, e.getMessage());
            }
            Thread.sleep(10000L);
        }
        return serviceIsHealthy;
    }

}
