import summaries

# test gen_summa_dict

d_pass = summaries.gen_summa_dict(True)
assert d_pass == {'bioimageio_spec_version': '0.4.8post1',
                  'bioimageio_core_version': '0.5.7',
                  'status': 'passed',
                  'name': 'Reproduce test outputs with DeepImageJ headless'}

d_weights = summaries.gen_summa_dict(False, "no-compatible-weights")
assert d_weights == {'bioimageio_spec_version': '0.4.8post1',
                     'bioimageio_core_version': '0.5.7',
                     'error': 'rdf does not have a compatible weight format',
                     'status': 'failed',
                     'name': 'Initial compatibility checks with DeepImageJ'}

d_comp = summaries.gen_summa_dict(False, "comparison")
assert d_comp == {'bioimageio_spec_version': '0.4.8post1',
                  'bioimageio_core_version': '0.5.7',
                  'error': 'Difference between CI and expected outputs is greater than threshold (CI produced an output image)',
                  'status': 'failed',
                  'name': 'Reproduce test outputs with DeepImageJ headless'}
