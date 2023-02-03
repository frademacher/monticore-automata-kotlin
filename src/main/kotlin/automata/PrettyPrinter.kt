package automata

import automata._ast.ASTAutomaton
import automata._ast.ASTState
import automata._ast.ASTTransition
import automata._visitor.AutomataHandler
import automata._visitor.AutomataTraverser
import de.monticore.prettyprint.IndentPrinter

/**
 * Pretty prints automatons. Use {@link #handle(ASTAutomaton)} to start a pretty print and get the result by using
 * {@link #getResult()}.
 */
class PrettyPrinter(
    val printer : IndentPrinter = IndentPrinter(),
    private var traverser : AutomataTraverser = AutomataMill.traverser()
) : AutomataHandler {
    init {
        traverser.setAutomataHandler(this)
    }

    /**
     * Gets the printed result.
     *
     * @return the result of the pretty print.
     */
    fun getResult() : String = printer.content

    override fun handle(node: ASTAutomaton) {
        printer.println("automaton ${node.name} {")
        printer.indent()
        traverser.traverse(node)
        printer.unindent()
        printer.println("}")
    }

    override fun traverse(node: ASTAutomaton) {
        // guarantee ordering: states before transitions
        node.stateList.forEach { it.accept(getTraverser()) }
        node.transitionList.forEach { it.accept(getTraverser()) }
    }

    override fun handle(node: ASTState) {
        printer.print("state ${node.name}")

        if (node.isInitial)
            printer.print(" <<initial>>")

        if (node.isFinal)
            printer.print(" <<final>>")

        printer.println(";")
        getTraverser().traverse(node)
    }

    override fun handle(node: ASTTransition) {
        printer.print(node.from)
        printer.print(" - ${node.input} > ")
        printer.print(node.to)
        printer.println(";")
        getTraverser().traverse(node)
    }

    override fun getTraverser() = traverser

    override fun setTraverser(traverser: AutomataTraverser) {
        this.traverser = traverser
    }

}