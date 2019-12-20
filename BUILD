package(default_visibility = ["//visibility:public"])

java_library(
	name = "duckutil_lib",
  srcs = glob(["src/*.java"]),
)

java_library(
  name = "lobstack_lib",
  srcs = glob(["src/lobstack/*.java"]),
  deps = [
    "@maven//:junit_junit",
    "@maven//:com_google_protobuf_protobuf_java",
    ":duckutil_lib",
  ],
)

java_library(
  name = "duckutil_jsonrpc_lib",
  srcs = glob(["src/jsonrpc/*.java"]),
  deps = [
    ":duckutil_lib",
    "@maven//:net_minidev_json_smart",
    "@maven//:com_thetransactioncompany_jsonrpc2_server",
    "@maven//:com_thetransactioncompany_jsonrpc2_base",
  ],
)    

java_library(
  name = "sql_lib",
  srcs = glob(["src/sql/*.java"]),
  deps = [
    "@maven//:commons_pool_commons_pool",
    "@maven//:commons_dbcp_commons_dbcp",
    ":duckutil_lib",
  ],
)

java_binary(
  name = "atomic_load",
  main_class = "duckutil.AtomicLoad",
  runtime_deps = [
    ":duckutil_lib",
  ],
)

java_test(
  name = "ziptest",
  srcs = ["test/ZipTest.java"],
  test_class = "ZipTest",
  size = "small",
  deps = [
    ":lobstack_lib",
    "@maven//:junit_junit",
  ],
)

java_test(
  name = "fusiontest",
  srcs = ["test/FusionInitiatorTest.java"],
  test_class = "FusionInitiatorTest",
  size = "medium",
  deps = [
    ":duckutil_lib",
    "@maven//:junit_junit",
  ],
)

java_test(
  name = "lobstacktest",
  srcs = ["test/LobstackTest.java"],
  test_class = "LobstackTest",
  size = "small",
  deps = [
    ":lobstack_lib",
    "@maven//:junit_junit",
  ],
)

