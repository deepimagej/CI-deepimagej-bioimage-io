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

script_prints = ["** script 1/2: TESTING WITH DEEPIMAGEJ HEADLESS\n",
                 "** script 2/2: COMPARING TO EXPECTED OUTPUT\n"]


def gen_messages(all_model_folders, k="start"):
    """Generates the messages for the start and end of the CI run"""
    tic = datetime.datetime.now()
    logs_msg = "Logs are in {} and {}\n\n".format(str(utils.get_in(FILES, ["logs", "out"]).absolute()),
                                                  utils.get_in(FILES, ["logs", "err"]).name)

    msgs = {"start": "STARTED TESTING THE {} MODELS WITH DEEPIMAGEJ IN FIJI AT {}\n\n".format(len(all_model_folders), tic),
            "end": "FINISHED TESTING THE {} MODELS WITH DEEPIMAGEJ IN FIJI AT {}\n\n".format(len(all_model_folders), tic)}

    return msgs[k] + logs_msg


def quote_arg(model_folder):
    """Quotes the argument to fiji script correctly for Windows"""
    return CONSTANTS["fiji-scripts-arg-name"] + "=" + "'" + model_folder + "'"


def compose_command(model_folder, script_name):
    """Creates a vector with the components of the command"""
    return [str(FILES["fiji-executable"].absolute())] + CONSTANTS["fiji-flags"] + [script_name, quote_arg(model_folder)]


def gen_execution_dicts(all_model_folders, scripts=script_names):
    """Generates a dict with execution info for every step (models). It has the commands and prints"""
    return list(map(lambda i_m: {"message":  "* MODEL {:3}/{:3}\n".format(i_m[0]+1, len(all_model_folders)),
                                 "cmd-vecs": list(map(lambda x: compose_command(i_m[1], x), scripts))},
                    enumerate(all_model_folders)))


def run_exec_step(execution_step, logs=FILES["logs"]):
    """Perform the commands for 1 execution step (1 model, 2 scripts)"""

    utils.print_and_log(execution_step["message"], [logs["out"], logs["err"]])
    for cmd, msg in zip(execution_step["cmd-vecs"], script_prints):
        utils.print_and_log(msg, [logs["out"], logs["err"]])
        try:
            # Shelling out happens here, some minutes for timeout (for bad models that might have infinite processing)
            c = subprocess.run(cmd, capture_output=True, text=True, timeout=CONSTANTS["timeout_s"])
            utils.print_and_log(c.stdout, [logs["out"]], pr=True)
            utils.print_and_log(c.stderr, [logs["err"]], pr=False)
        except subprocess.TimeoutExpired as e:
            utils.print_and_log("ERROR: Process timed out {:.1f} min ({:.0f} s).".format(CONSTANTS["timeout_s"]/60,
                                                                                         CONSTANTS["timeout_s"]) +
                                " Maybe running the model in deepimagej produces and infinite loop.\n",
                                [logs["out"], logs["err"]], pr=True)


def test_models_in_fiji(all_model_folders, scripts=script_names, logs_dict=FILES["logs"]):
    """Runs the commands from the execution-dict. Logs outputs"""
    tic = datetime.datetime.now()
    logs = [logs_dict["out"], logs_dict["err"]]
    # reset contents of log files
    for log in logs:
        f = open(log, "w")
        f.close()
    # run
    utils.print_and_log(gen_messages(all_model_folders, "start"), logs)
    list(map(lambda x: run_exec_step(x, logs_dict), gen_execution_dicts(all_model_folders, scripts)))
    utils.print_and_log(gen_messages(all_model_folders, "end"), logs)
    utils.print_and_log("Total Time Taken: {}\n".format(datetime.datetime.now() - tic), logs)
