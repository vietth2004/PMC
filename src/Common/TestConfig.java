package Common;

import utils.Utils;

import java.io.File;

public class TestConfig
{
    public static String PROJECT_PATH = "F:\\VietData\\GitLab\\bai10\\data-test\\Sample_for_R1_2";
    public static String WORKSPACE = "Workspace";
    public static String EXE_PATH = PROJECT_PATH + "\\exe";
    public static String TEST_DRIVER_PATH = PROJECT_PATH + "\\TestDriver";
    public static String COMPILE_OUTPUT = PROJECT_PATH + "\\BuildOutput";
    public static String LINK_OUTPUT = PROJECT_PATH + "\\LinkOutput";
    public static String TESTPATH_FILE = PROJECT_PATH + "\\Testpath";
    public static String INSTRUMENTED_CODE = PROJECT_PATH + "\\InstrumentedCode";

    public static String TESTCASE_NAME = "TestCase";
    public static String COMPILE_COMMAND_TEMPLATE = "g++ -std\u003dc++14 \"%s\" -c -o\"%s\"  -w "; // -lgtest_main  -lgtest
    public static String LINK_COMMAND_TEMPLATE = "g++ -std\u003dc++14 \"%s\" -o\"%s\" -w "; //-lgtest_main  -lgtest

    //Command
    //g++ -Wall -o main main.cpp -I/usr/local/include -L/usr/local/lib -lopencv_calib3d -lopencv_core
    // -lopencv_features2d -
    //https://eitguide.net/huong-dan-build-ma-nguon-cc-voi-terminal-nang-cao/
    //https://www.stdio.vn/modern-cpp/build-chuong-trinh-c-c-bang-gcc-nang-cao-s16nHh
//    Cần chỉ định cho gcc đường dẫn chứa các file header của thư viện sử dụng -I
//    Cần chỉ định cho gcc đường dẫn chứa các file library của thư viện sử dụng -L
//    Cần link các module thư viện liên kết.
    //Ví dụ: gcc -c .\src\strscpy.c -I .\include
    //https://github.com/libuv/libuv
    // Download site: https://dist.libuv.org/dist/

    public static String UET_IGNORE_FILE = ".uetignore";
    public static String C_EXTENTION = ".c";
    public static String CPP_EXTENTION = ".cpp";
    public static String EXE_EXTENTION = ".exe";
    public static String TESTPATH_EXTENTION = ".tp";
    public static String UET_TEST_CASE_NAME = "UET_test_case_name";
    public static String TOOL_TITLE = "VNU-UET test data generation tool for C/C++";
    public static String MAKEFILE_PATH = "\\Make";

    public static String PATH_SELECTION_STRATEGY = "ImprovedSDART"; //"DART_BFS", "SDART", "ImprovedSDART"

    public static String SOLVER_Z3_PATH = "./local/z3/bin/z3.exe";

    public static String SDART_TESTCASE_NAME = "Testcase";

    public static String PATH_SEPARATOR = "\\";

    public static void SetProjectPath(String projectPath)
    {
        PROJECT_PATH = projectPath;
        String parentPath = new File(PROJECT_PATH).getParent();

        EXE_PATH = parentPath + "\\" + WORKSPACE + "\\exe";
        Utils.createFolder(EXE_PATH);

        TEST_DRIVER_PATH = parentPath + "\\" + WORKSPACE + "\\TestDriver";
        Utils.createFolder(TEST_DRIVER_PATH);

        COMPILE_OUTPUT = parentPath + "\\" + WORKSPACE + "\\BuildOutput";
        Utils.createFolder(COMPILE_OUTPUT);

        LINK_OUTPUT = parentPath + "\\" + WORKSPACE + "\\LinkOutput";
        Utils.createFolder(LINK_OUTPUT);

        TESTPATH_FILE = parentPath + "\\" + WORKSPACE + "\\Testpath";
        Utils.createFolder(TESTPATH_FILE);

        INSTRUMENTED_CODE = parentPath + "\\" + WORKSPACE + "\\InstrumentedCode";
        Utils.createFolder(INSTRUMENTED_CODE);

        MAKEFILE_PATH = parentPath + "\\" + WORKSPACE + "\\Make";
        Utils.createFolder(MAKEFILE_PATH);
    }
}
