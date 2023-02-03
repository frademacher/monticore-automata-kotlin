package automata

import automata._ast.ASTAutomaton
import automata._cocos.AutomataCoCoChecker
import automata._parser.AutomataParser
import de.se_rwth.commons.logging.Log
import org.antlr.v4.runtime.RecognitionException
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.ParseException
import java.io.IOException
import kotlin.system.exitProcess
import automata.AutomataTool as AutomataToolTOP

/**
 * Main function of the Tool
 *
 * Arguments expected:
 * * input automaton file.
 * * the path to store the symbol table
 *
 * @param args
 */
fun main(args: Array<String>) {
    // delegate main to instantiatable method for better integration, reuse, etc.
    RunnableAutomataTool().run(args)
}

private const val TOOL_NAME = "AutomataTool"

/**
 * Main class for the Automata DSL tool.
 *
 */
private class RunnableAutomataTool : AutomataToolTOP() {
    /**
     * Run implements the main method of the Automata tool workflow:
     *
     * Arguments expected:
     * * input automaton file.
     * * the path to store the symbol table
     *
     * @param args
     */
    override fun run(args: Array<out String>) {
        // use normal logging (no DEBUG, TRACE)
        AutomataMill.init()
        Log.ensureInitalization()

        val options = initOptions()
        val cmd = try {
                //create CLI Parser and parse input options from commandline
                DefaultParser().parse(options, args)
            } catch (e: ParseException) {
                // e.getMessage displays the incorrect input-parameters
                Log.error("0xEE752 Could not process $TOOL_NAME parameters: ${e.message}")
                return
            }

        when {
            //version: when --version
            cmd.hasOption("v") -> {
                printVersion()
                //do not continue, when version is printed.
                return
            }

            //help: when --help
            cmd.hasOption("h") || !cmd.hasOption("i") -> {
                printHelp(options)
                //do not continue, when help is printed.
                return
            }
        }

        Log.info("Automata DSL Tool", TOOL_NAME)

        AutomataMill.globalScope().fileExt = "aut"
        val model  = cmd.getOptionValue("i")
        val ast = parse(model) ?: exitProcess(1)
        Log.info("$model parsed successfully!", TOOL_NAME)
        val modelTopScope = createSymbolTable(ast)
        // can be used for resolving things in the model
        val aSymbol = modelTopScope.resolveState("Ping")
        if (aSymbol.isPresent)
            Log.info("""Resolved state symbol "Ping"; FQN = ${aSymbol.get()} """, TOOL_NAME)
        else
            Log.info("""This automaton does not contain a state called "Ping";""", TOOL_NAME)
        runDefaultCoCos(ast)

        if (cmd.hasOption("s")) {
            val storeLocation = cmd.getOptionValue("s")
            storeSymbols(modelTopScope, storeLocation)
        }

        // analyze the model with a visitor
        val cs = CountStates()
        val traverser = AutomataMill.traverser()
        traverser.add4Automata(cs)
        ast.accept(traverser)
        Log.info("Automaton has ${cs.count} states", TOOL_NAME)
        prettyPrint(ast, "")
    }

    /**
     * Parse the model contained in the specified file.
     *
     * @param model - file to parse
     * @return
     */
    override fun parse(model: String) =
        try {
            val parser = AutomataParser()
            val optAutomaton = parser.parse(model)
            if (!parser.hasErrors() && optAutomaton.isPresent)
                optAutomaton.get()
            else {
                Log.error("0xEE840 Model could not be parsed.")
                null
            }
        } catch (e: RecognitionException) {
            Log.error("0xEE640 Failed to parse $model", e)
            null
        } catch (e: IOException) {
            Log.error("0xEE640 Failed to parse $model", e)
            null
        }

    override fun runDefaultCoCos(ast: ASTAutomaton) {
        // setup context condition infrastructure
        val checker = AutomataCoCoChecker()

        // add a custom set of context conditions
        checker.addCoCo(StateNameStartsWithCapitalLetter())
        checker.addCoCo(AtLeastOneInitialAndFinalState())
        checker.addCoCo((TransitionSourceExists()))

        // check the CoCos
        checker.checkAll(ast)
    }

    override fun prettyPrint(ast: ASTAutomaton, file: String) {
        // execute a pretty printer
        val pp = PrettyPrinter()
        val traverser = AutomataMill.traverser()
        traverser.setAutomataHandler(pp)
        ast.accept(traverser)
        Log.info("Pretty printing automaton into console:", TOOL_NAME)
        // print the result
        Log.println(pp.getResult())
    }
}