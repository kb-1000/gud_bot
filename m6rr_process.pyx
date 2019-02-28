cimport cython
import numpy
cimport numpy

ctypedef long snowflake

cdef struct m6_item:
    long level
    snowflake user

async def convert(list it):
    cdef list it2 = it
    cdef dict d
    cdef str idstr
    cdef long level
    for d in it2:
        idstr = d["id"]
        level = d["level"]
