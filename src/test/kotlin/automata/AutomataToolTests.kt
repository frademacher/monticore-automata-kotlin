package automata

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AutomataToolTests {
    @Test
    fun printVersion() {
        assertEquals(
            runTool("-v"),
            "AutomataTool, version 1.0-SNAPSHOT, based on MontiCore version 7.5.0-SNAPSHOT"
        )
    }

    private fun runTool(vararg args: String) : String {
        val stdout = System.out
        val out = ByteArrayOutputStream()
        System.setOut(PrintStream(out))
        main(arrayOf(*args))
        System.setOut(stdout)
        return out.toString(StandardCharsets.UTF_8).trim()
    }

    @Test
    fun printHelp() {
        assertEquals(
            runTool("-h"),
            "usage: AutomataTool\n" +
            " -h,--help                  Prints this help dialog\n" +
            " -i,--input <file>          Reads the source file (mandatory) and parses the\n" +
            "                            contents\n" +
            " -path <arg>                Sets the artifact path for imported symbols, space\n" +
            "                            separated.\n" +
            " -pp,--prettyprint <file>   Prints the AST to stdout or the specified file\n" +
            "                            (optional)\n" +
            " -r,--report <dir>          Prints reports of the artifact to the specified\n" +
            "                            directory.\n" +
            " -s,--symboltable <file>    Serialized the Symbol table of the given artifact.\n" +
            " -v,--version               Prints version information"
        )
    }

    @Test
    fun pingPong() {
        assertEquals(
            runTool("-i", "example/PingPong.aut"),
            "[INFO]  AutomataTool Automata DSL Tool\n" +
            "[INFO]  AutomataTool example/PingPong.aut parsed successfully!\n" +
            "[INFO]  AutomataTool This automaton does not contain a state called \"Ping\";\n" +
            "[INFO]  AutomataTool Automaton has 3 states\n" +
            "[INFO]  AutomataTool Pretty printing automaton into console:\n" +
            "automaton PingPong {\n" +
            "  state NoGame <<initial>>;\n" +
            "  state Ping;\n" +
            "  state Pong <<final>>;\n" +
            "  NoGame - startGame > Ping;\n" +
            "  Ping - stopGame > NoGame;\n" +
            "  Pong - stopGame > NoGame;\n" +
            "  Ping - returnBall > Pong;\n" +
            "  Pong - returnBall > Ping;\n" +
            "}"
        )
    }
}