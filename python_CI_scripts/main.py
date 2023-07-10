# info for arg parsing https://docs.python.org/3/library/argparse.html
from config import ROOTS
import actions
import collection

from functools import partial
import argparse

parser = argparse.ArgumentParser(
    prog="main.py",
    description="Python CI for testing bioimagio models in deepimagej",
    epilog="more info at: https://github.com/ivan-ea/CI-deepimagej-bioimage-io/blob/master/python_CI_scripts/Readme.md")

parser.add_argument("action", choices=["init", "download", "reproduce"], default="init", help="""
    #  init (DEFAULT) Initial checks & generate folder structures and files for the compatible models to test.\n
    #  download       Populate model folders (download files). Build args for DeepImagej headless.\n
    #  reproduce      Run the models on Fiji with DeepImageJ headless. Create tests summaries.\n""")
parser.add_argument('-j', '--json-file', action='store', default=ROOTS["pending-matrix-root"] / "use_cases.json")
parser.add_argument('-s', '--json-string', action='store')


args = parser.parse_args()

print("args.json_file is:", args.json_file)
print("Args are:", args)

input_json = collection.file_json_2_vector(args.json_file) if args.json_string is None else collection.str_json_2_vector(args.json_string)
print("\nInput json is:\n", input_json)

action_fns = {"init": partial(actions.initial_pipeline, ini_return=False, input_json=input_json),
              "download": partial(actions.download_pipeline, input_json=input_json),
              "reproduce": actions.reproduce_pipeline}

action_fns[args.action]()
