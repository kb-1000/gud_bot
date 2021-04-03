pip-compile -U --allow-unsafe -v --annotate --generate-hashes -o requirements-dev.txt requirements-dev.in
pip-compile -U --allow-unsafe -v --annotate --generate-hashes -o requirements.txt requirements.in
