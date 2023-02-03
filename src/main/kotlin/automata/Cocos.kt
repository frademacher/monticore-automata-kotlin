package automata

import automata._ast.ASTAutomaton
import automata._ast.ASTState
import automata._ast.ASTTransition
import automata._cocos.AutomataASTAutomatonCoCo
import automata._cocos.AutomataASTStateCoCo
import automata._cocos.AutomataASTTransitionCoCo
import de.se_rwth.commons.logging.Log

class AtLeastOneInitialAndFinalState : AutomataASTAutomatonCoCo {
    override fun check(automaton: ASTAutomaton) {
        val initialState = automaton.stateList.any { it.isInitial }
        val finalState = automaton.stateList.any { it.isFinal }
        if (!initialState || !finalState)
            // Issue error...
            Log.error("0xA1117 An automaton must have at least one initial and one final state.",
                automaton._SourcePositionStart)
    }
}

class StateNameStartsWithCapitalLetter : AutomataASTStateCoCo {
    override fun check(state: ASTState) {
        if (!state.name[0].isUpperCase())
            Log.warn("""0xADD21 State name "${state.name}" is not capitalized.""", state._SourcePositionStart)
    }
}

class TransitionSourceExists : AutomataASTTransitionCoCo {
    override fun check(node: ASTTransition) {
        val sourceState = node.enclosingScope.resolveState(node.from)
        if (!sourceState.isPresent)
            // Issue error...
            Log.error("0xADD31 Source state of transition missing.", node._SourcePositionStart)
    }
}