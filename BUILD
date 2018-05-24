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

