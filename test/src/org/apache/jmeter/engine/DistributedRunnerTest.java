/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.jmeter.engine;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class DistributedRunnerTest extends junit.framework.TestCase {

    public static void createJmeterEnv() throws IOException {
        File propsFile;
        try {
            propsFile = File.createTempFile("jmeter-plugins", ".properties");
            propsFile.deleteOnExit();
            JMeterUtils.loadJMeterProperties(propsFile.getAbsolutePath());
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
        JMeterUtils.setLocale(new Locale("ignoreResources"));
    }

    public void testSuccess() throws Exception {
        createJmeterEnv();
        JMeterUtils.setProperty(DistributedRunner.RETRIES_NUMBER, "1");
        JMeterUtils.setProperty(DistributedRunner.CONTINUE_ON_FAIL, "false");
        DistributedRunnerEmul obj = new DistributedRunnerEmul();
        obj.engines.add(new EmulatorEngine());
        obj.engines.add(new EmulatorEngine());
        List<String> hosts = Arrays.asList("test1", "test2");
        obj.init(hosts, new HashTree());
        obj.start();
        obj.shutdown(hosts);
        obj.stop(hosts);
        obj.exit(hosts);
    }

    public void testFailure1() throws Exception {
        createJmeterEnv();
        JMeterUtils.setProperty(DistributedRunner.RETRIES_NUMBER, "2");
        JMeterUtils.setProperty(DistributedRunner.RETRIES_DELAY, "1");
        JMeterUtils.setProperty(DistributedRunner.CONTINUE_ON_FAIL, "true");
        DistributedRunnerEmul obj = new DistributedRunnerEmul();
        List<String> hosts = Arrays.asList("test1", "test2");
        try {
            obj.init(hosts, new HashTree());
            fail();
        } catch (RuntimeException ignored) {
        }
        obj.start();
        obj.shutdown(hosts);
        obj.stop(hosts);
        obj.exit(hosts);
    }

    public void testFailure2() throws Exception {
        createJmeterEnv();
        JMeterUtils.setProperty(DistributedRunner.RETRIES_NUMBER, "1");
        JMeterUtils.setProperty(DistributedRunner.RETRIES_DELAY, "1");
        JMeterUtils.setProperty(DistributedRunner.CONTINUE_ON_FAIL, "false");
        DistributedRunnerEmul obj = new DistributedRunnerEmul();
        List<String> hosts = Arrays.asList("test1", "test2");
        try {
            obj.init(hosts, new HashTree());
            fail();
        } catch (RuntimeException ignored) {
        }
    }

    public void testFailure3() throws Exception {
        createJmeterEnv();
        JMeterUtils.setProperty(DistributedRunner.RETRIES_NUMBER, "1");
        JMeterUtils.setProperty(DistributedRunner.RETRIES_DELAY, "1");
        JMeterUtils.setProperty(DistributedRunner.CONTINUE_ON_FAIL, "true");
        DistributedRunnerEmul obj = new DistributedRunnerEmul();
        List<String> hosts = Arrays.asList("test1", "test2");
        try {
            obj.init(hosts, new HashTree());
            fail();
        } catch (RuntimeException ignored) {
        }
        obj.start(hosts);
        obj.shutdown(hosts);
        obj.stop(hosts);
        obj.exit(hosts);
    }

    private class DistributedRunnerEmul extends DistributedRunner {
        public List<EmulatorEngine> engines = new LinkedList<EmulatorEngine>();

        @Override
        protected JMeterEngine createEngine(String address) throws RemoteException, NotBoundException, MalformedURLException {
            EmulatorEngine engine = engines.remove(0);
            engine.setHost(address);
            return engine;
        }
    }

    private static class EmulatorEngine implements JMeterEngine {
        private static final Logger log = LoggingManager.getLoggerForClass();
        private String host;

        public EmulatorEngine() {
            log.debug("Creating emulator " + host);
        }

        @Override
        public void configure(HashTree testPlan) {
            log.debug("Configuring " + host);
        }

        @Override
        public void runTest() throws JMeterEngineException {
            log.debug("Running " + host);
        }

        @Override
        public void stopTest(boolean now) {
            log.debug("Stopping " + host);
        }

        @Override
        public void reset() {
            log.debug("Resetting " + host);
        }

        @Override
        public void setProperties(Properties p) {
            log.debug("Set properties " + host);
        }

        @Override
        public void exit() {
            log.debug("Exitting " + host);
        }

        @Override
        public boolean isActive() {
            log.debug("Check if active " + host);
            return false;
        }

        public void setHost(String host) {
            this.host = host;
        }
    }
}