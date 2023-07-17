"""
Run the 2 scripts: inference with DeepImageJ and comparison with Fiji
Shelling out with python from windows to run the fiji commands
"""

from config import FILES, CONSTANTS
import utils

from pathlib import Path
import datetime


script_names = list(map(lambda x: str(Path(".", "fiji_scripts", x).absolute()),
                        ["test_1_with_deepimagej.py", "create_output_metrics.py"]))

script_prints = ["-- script 1/2: TESTING WITH DEEPIMAGEJ HEADLESS\n",
                 "-- script 2/2: COMPARING TO EXPECTED OUTPUT\n"]


def gen_messages(all_model_folders, k="start", t_ini=datetime.datetime.now()):
    """Generates the messages for the start and end of the CI run"""
    tic = datetime.datetime.now()
    logs_msg = "Logs are in {} and {}\n\n".format(str(utils.get_in(FILES, ["logs", "out"]).absolute()),
                                                  utils.get_in(FILES, ["logs", "err"]).name)

    msgs = {"start": "STARTED TESTING THE {} MODELS WITH DEEPIMAGEJ IN FIJI AT {}\n\n".format(len(all_model_folders), tic),
            "end": "FINISHED TESTING THE {} MODELS WITH DEEPIMAGEJ IN FIJI AT {}\nTotal time taken: {}\n".format(
                len(all_model_folders), tic, tic - t_ini)}

    return msgs[k] + logs_msg


def quote_arg(model_folder):
    """Quotes the argument to fiji script correctly for Windows"""
    return CONSTANTS["fiji-scripts-arg-name"] + "=" + "'" + model_folder + "'"


def compose_command(model_folder, script_name):
    """Creates a vector with the components of the command"""
    return []


