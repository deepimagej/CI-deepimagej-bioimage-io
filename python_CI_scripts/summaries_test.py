from config import FILES
import summaries
import models_test
import errors_test

from pathlib import Path
import yaml


# test gen_summa_dict

d_pass = summaries.gen_summa_dict(True)
assert d_pass == {'bioimageio_spec_version': '0.4.9',
                  'bioimageio_core_version': '0.5.9',
                  'status': 'passed',
                  'name': 'Reproduce test outputs with DeepImageJ headless'}

d_weights = summaries.gen_summa_dict(False, "no-compatible-weights")
assert d_weights == {'bioimageio_spec_version': '0.4.9',
                     'bioimageio_core_version': '0.5.9',
                     'error': 'rdf does not have a compatible weight format',
                     'status': 'failed',
                     'name': 'Initial compatibility checks with DeepImageJ'}

d_comp = summaries.gen_summa_dict(False, "comparison")
assert d_comp == {'bioimageio_spec_version': '0.4.9',
                  'bioimageio_core_version': '0.5.9',
                  'error': 'Difference between CI and expected outputs is greater than threshold (CI produced an output image)',
                  'status': 'failed',
                  'name': 'Reproduce test outputs with DeepImageJ headless'}

# test write_test_summary
mock_path = Path(".", "test_outputs", "test_summary_folder")
mock_model = {"paths": {"summa-path": mock_path}}
summaries.write_test_summary(d_pass, mock_model, verb=True)

with open(mock_path / "test_summary.yaml", "r", encoding="utf-8") as f:
    data = yaml.safe_load(f)

assert data == d_pass

# test write_summaries_from_error
# wipe out contents of log file
f = open(FILES["summa-readme"], "w")
f.close()

error_struct = errors_test.models_discriminated["error-found"]

# summaries.write_summaries_from_error(list(error_struct.items())[1]) # testing on only 1 of the errors

list(map(lambda x: summaries.write_summaries_from_error(x), error_struct.items()))

# for writing test summaries for all models
#list(map(lambda x: summaries.write_summaries_from_error(x), errors_test.all_models_discriminated["error-found"].items()))