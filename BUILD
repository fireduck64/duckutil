package(default_visibility = ["//visibility:public"])

java_library(
	name = "duckutil_lib",
  srcs = glob(["src/*.java"]),
)

java_library(
  name = "lobstack_lib",
  srcs = glob(["src/lobstack/*.java"]),
  deps = [
    "@junit_junit//jar",
    "@protobuf//jar",
    ":duckutil_lib",
  ],
)

java_library(
  name = "duckutil_jsonrpc_lib",
  srcs = glob(["src/jsonrpc/*.java"]),
  deps = [
    ":duckutil_lib",
    "@jsonrpc2_server//jar",
    "@jsonrpc2_base//jar",
    "@json_smart//jar",
    "@accessors_smart//jar",
    "@asm//jar",
  ],
)    

java_library(
  name = "sql_lib",
  srcs = glob(["src/sql/*.java"]),
  deps = [
    "@commons_pool//jar",
    "@commons_dbcp//jar",
  ],
)

java_test(
  name = "ziptest",
  srcs = ["test/ZipTest.java"],
  test_class = "ZipTest",
  size = "small",
  deps = [
    ":lobstack_lib",
    "@junit_junit//jar",
  ],
)

java_test(
  name = "lobstacktest",
  srcs = ["test/LobstackTest.java"],
  test_class = "LobstackTest",
  size = "small",
  deps = [
    ":lobstack_lib",
    "@junit_junit//jar",
  ],
)

