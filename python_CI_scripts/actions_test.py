
import actions

json_input = [{'resource_id': '**', 'version_id': '**'}]

keep_testing = actions.initial_pipeline(True, json_input)

assert len(keep_testing) > 100

