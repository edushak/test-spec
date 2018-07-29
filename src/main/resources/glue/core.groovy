import cucumber.api.groovy.Hooks
import org.edushak.testspec.TestSpecWorld

Hooks.World {
    def world = new TestSpecWorld()
    world.binding = this.binding
    TestSpecWorld.currentWorld = world
}