import Testing
import Starlark

@Suite struct StarlarkExportTests {
    @Test func swiftModuleLoads() throws {
        #expect(true, "Starlark swift module imported cleanly")
    }
}
