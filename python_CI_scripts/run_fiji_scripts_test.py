import datetime

from run_fiji_scripts import *
import communicate as comm

from models_test import a_model_record, pt_model_record, tf_model_record
from communicate_test import affable_sark_model, joyful_deer_model, creative_panda_model

model_paths = list(map(lambda x: comm.get_model_folder_str(x), [a_model_record, pt_model_record, tf_model_record,
                                                                affable_sark_model, joyful_deer_model,
                                                                creative_panda_model]))

# not so many assertions, outputs depend on absolute path of computer where tests are running

print("Testing script_names")
print(script_names[0])
print(script_names[1])
print()

print("Testing gen_messages")
t = datetime.datetime.now()
print(gen_messages(model_paths, k="start"))
print(gen_messages(model_paths, k="end", t_ini=t))

print("Testing quote_arg")