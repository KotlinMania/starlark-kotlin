import Testing
import Starlark

@Suite struct StarlarkExportTests {
    @Test func swiftModuleLoads() throws {
        #expect(Bool(true), "Starlark swift module imported cleanly")
    }
}
