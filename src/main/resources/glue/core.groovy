import com.sun.javafx.PlatformUtil
import cucumber.api.groovy.Hooks
import org.apache.commons.lang.time.DateUtils
import org.edushak.testspec.TestSpecWorld
import org.edushak.testspec.util.Helper

import java.util.concurrent.TimeUnit

import static cucumber.api.groovy.EN.*

Hooks.World {
    def world = new TestSpecWorld()
    world.binding = this.binding
    TestSpecWorld.currentWorld = world
}

When(~/I wait for (\d+) (milliseconds|seconds|minutes)$/) { long duration, String timeUnit ->
    long msDuration = TimeUnit."${timeUnit.toUpperCase()}".toMillis(duration)
    if (msDuration > DateUtils.MILLIS_PER_HOUR) {
        throw new Exception("Waiting duration should not exceed 60 min")
    }
    sleep(msDuration)
}

When(~/^I execute code "([^"]*)"( asynchronously)?:$/) { String descr, String async, String code ->
    executeCodeFacade(async, code)
}

When(~/I execute command: (.*)$/) { String command -> // ( asynchronously)?   String async,
    command = normalizeParameter(command, true)
    if (Helper.isClosure(command)) {
        command = Helper.evaluate(command, binding)
    }
    commandResults = executeCommand(command) // , async
    binding.setVariable('_lastCommandResult', commandResults)
}
Then(~/last command exit code should be (\d+)/) { int expectedExitCode ->
    assert commandResults.exitCode == expectedExitCode
}
Map executeCommand(String command, async = null) {
    List commandForOs = getOsPrefix() + command
    def builder = new ProcessBuilder(commandForOs)
    Process p = builder.start()

    def stdOutBuilder = new StringBuilder(),
        stdErrBuilder = new StringBuilder()
    p.waitForProcessOutput(stdOutBuilder, stdErrBuilder)
    int exitCode = p.waitFor()
    return [
        command : commandForOs,
        exitCode: exitCode,
        stdOut  : stdOutBuilder.toString().trim(),
        stdErr  : stdErrBuilder.toString().trim(),
    ]
}

static List getOsPrefix() {
    if (PlatformUtil.isWindows()) {
        ['cmd.exe', '/c']
    } else {
        ['sh', '-c']
    }
}
