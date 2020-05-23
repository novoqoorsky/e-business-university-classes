
import { VisibilityFilters } from '../actions'

const visibilityFilter = (action, state = VisibilityFilters.SHOW_ALL) => {
    if (action.type === 'SET_VISIBILITY_FILTER') {
        return action.filter
    }
    return state
}

export default visibilityFilter