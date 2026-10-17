package gg.pearl

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import com.lemonappdev.konsist.api.verify.assertFalse
import kotlin.test.Test

class ArchitectureTest {

    @Test
    fun `domain layer must not depend on frameworks or infrastructure`() {
        Konsist
            .scopeFromPackage("gg.pearl.model..")
            .files
            .assertFalse { file ->
                file.hasImport { import ->
                    import.name.startsWith("io.ktor") ||
                    import.name.startsWith("org.jetbrains.exposed") ||
                    import.name.startsWith("com.zaxxer") ||
                    import.name.startsWith("org.flywaydb") ||
                    import.name.startsWith("java.sql") ||
                    import.name.startsWith("gg.pearl.controller") ||
                    import.name.startsWith("gg.pearl.service") ||
                    import.name.startsWith("gg.pearl.repository") ||
                    import.name.startsWith("gg.pearl.plugins")
                }
            }
    }

    @Test
    fun `clean architecture layers have correct dependency directions`() {
        Konsist
            .scopeFromProduction()
            .assertArchitecture {
                val domain = Layer("Domain", "gg.pearl.model..")
                val service = Layer("Service", "gg.pearl.service..")
                val repository = Layer("Repository", "gg.pearl.repository..")
                val controller = Layer("Controller", "gg.pearl.controller..")

                domain.dependsOnNothing()
                service.dependsOn(domain, repository)
                repository.dependsOn(domain)
                controller.dependsOn(service, domain)
            }
    }

    @Test
    fun `services must not depend on web framework or controllers`() {
        Konsist
            .scopeFromPackage("gg.pearl.service..")
            .files
            .assertFalse { file ->
                file.hasImport { import ->
                    import.name.startsWith("io.ktor.server") ||
                    import.name.startsWith("gg.pearl.controller")
                }
            }
    }

    @Test
    fun `controllers must not depend directly on database tables or repository implementations`() {
        Konsist
            .scopeFromPackage("gg.pearl.controller..")
            .files
            .assertFalse { file ->
                file.hasImport { import ->
                    import.name.startsWith("gg.pearl.repository.tables") ||
                    import.name.endsWith("RepositoryImpl")
                }
            }
    }

    @Test
    fun `repository interfaces must not depend on database framework`() {
        Konsist
            .scopeFromPackage("gg.pearl.repository")
            .interfaces()
            .assertFalse { inter ->
                inter.containingFile.hasImport { import ->
                    import.name.startsWith("org.jetbrains.exposed") ||
                    import.name.startsWith("java.sql")
                }
            }
    }
}
