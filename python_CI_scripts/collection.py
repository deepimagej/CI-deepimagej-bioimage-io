"""Handles the bioimageio collection of rdf's"""

from config import ROOTS
from pathlib import Path
import json
import itertools


def str_json_2_vector(str_json):
    """Returns the parsed list of resources/versions to test, given a raw json string"""
    try:
        parsed = json.loads(str_json)
        return parsed["include"]
    except json.JSONDecodeError as e:
        print("String is not a valid json")
        return []


def file_json_2_vector(json_file):
    """Returns the parsed list of resources/versions to test, given a json file"""
    with open(json_file, "r") as file:
        data = file.read().rstrip()
        return str_json_2_vector(data)


def filter_rdfs(paths):
    """Keeps only the rdf.yaml from a seq of paths"""
    return list(filter(lambda x: x.name == "rdf.yaml", paths))


def resources_2_paths(resource_map, root=ROOTS["collection-root"]):
    """Takes a resource/version to test and returns list of rdf path(s) (multiple if globbing).
    If only 1 argument is given, uses COLLECTION-ROOT as root path"""
    r_id = resource_map["resource_id"]
    v_id = resource_map["version_id"]

    if r_id == "**":
        return filter_rdfs(root.glob("**/*"))
    elif v_id == "**":
        return filter_rdfs((root / r_id).glob("**/*"))
    else:
        return [Path(root / r_id / v_id / "rdf.yaml")]


def get_rdfs_to_test(resources_vector, root=ROOTS["collection-root"]):
    """ Compiles a list of rdf paths that need to be tested, given a list of resource/versions maps.
    If only 1 argument is given, uses COLLECTION ROOT as root path"""
    unflattened = list(map(lambda x: resources_2_paths(x, root), resources_vector))
    return list(set(itertools.chain(*unflattened)))
