import utils
import errors
import models
import models_test

# test the initial checks
non_model = models.build_model(models_test.not_a_model_rdf)

assert not errors.is_model(non_model)
assert errors.is_model(models_test.a_model_record)

assert not errors.is_no_run_mode(models_test.model_with_run_mode)
assert errors.is_no_run_mode(models_test.a_model_record)

assert not errors.is_any_compatible_weight(non_model)
assert not errors.is_any_compatible_weight(models_test.a_model_record)
assert errors.is_any_compatible_weight(models_test.pt_model_record)
assert errors.is_any_compatible_weight(models_test.tf_model_record)
