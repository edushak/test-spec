package org.edushak.testspec

import cucumber.runtime.ClassFinder
import cucumber.runtime.Runtime
import cucumber.runtime.RuntimeOptions
import cucumber.runtime.io.MultiLoader
import cucumber.runtime.io.ResourceLoader
import cucumber.runtime.io.ResourceLoaderClassFinder

class Main {
    public Main() {
    }

    public static void main(String[] argv) throws Throwable {
        byte exitstatus = run(argv, Thread.currentThread().getContextClassLoader());
        System.exit(exitstatus);
    }

    public static byte run(String[] argv, ClassLoader classLoader) throws IOException {
        RuntimeOptions runtimeOptions = new RuntimeOptions(new ArrayList(Arrays.asList(argv)));
        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        Runtime runtime = new Runtime(resourceLoader, classFinder, classLoader, runtimeOptions);
        runtime.run();
        return runtime.exitStatus();
    }

}
