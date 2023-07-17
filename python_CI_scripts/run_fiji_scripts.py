"""
Run the 2 scripts: inference with DeepImageJ and comparison with Fiji
Shelling out with python from windows to run the fiji commands
"""

from config import FILES, CONSTANTS
import utils

from pathlib import Path
import datetime
import subprocess

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
    return [str(FILES["fiji-executable"].absolute())] + CONSTANTS["fiji-flags"] + [script_name, quote_arg(model_folder)]


def gen_execution_dicts(all_model_folders):
    """Generates a dict with execution info for every step (models). It has the commands and prints"""
    return list(map(lambda i_m: {"message":  "- MODEL {:3}/{:3}".format(i_m[0]+1, len(all_model_folders)),
                                 "cmd-vecs": list(map(lambda x: compose_command(i_m[1], x), script_names))},
                    enumerate(all_model_folders)))


def run_exec_step(execution_step, logs=FILES["logs"]):
    """Perform the commands for 1 execution step (1 model, 2 scripts)"""

    utils.print_and_log(execution_step["message"], [logs["out"], logs["err"]])
    for cmd, msg in zip(execution_step["cmd-vecs"], script_prints):
        utils.print_and_log(msg, [logs["out"], logs["err"]])
        c = subprocess.run(cmd, capture_output=True, text=True)  # <- Shelling out happens here
        utils.print_and_log(c.stdout, [logs["out"]], pr=True)
        utils.print_and_log(c.stderr, [logs["err"]], pr=False)


def test_models_in_fiji(all_model_folders, logs=FILES["logs"]):
    """Runs the commands from the execution-dict. Logs outputs"""
    tic = datetime.datetime.now()
    # reset contents of log files
    return