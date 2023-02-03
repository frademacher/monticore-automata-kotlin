package automata

import automata._ast.ASTState
import automata._visitor.AutomataVisitor2

/**
 * Counts the states of an automaton.
 *
 */
class CountStates : AutomataVisitor2 {
    var count: Int = 0
        private set

    override fun visit(node: ASTState) {
        count++
    }
}