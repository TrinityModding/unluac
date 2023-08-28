package me.hydos.unluac.test;

import me.hydos.unluac.Configuration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class UnluacTests {
    public static final List<UnLuaCTest> TESTS = List.of(
            new UnLuaCTest("assign"),
            new UnLuaCTest("literal"),
            new UnLuaCTest("number01"),
            new UnLuaCTest("number02"),
            new UnLuaCTest("number03"),
            new UnLuaCTest("number04"),
            new UnLuaCTest("multiassign"),
            new UnLuaCTest("multiassign02"),
            new UnLuaCTest("multiassign03"),
            new UnLuaCTest("multiassign04"),
            new UnLuaCTest("multiassign05"),
            new UnLuaCTest("multiassign06"),
            new UnLuaCTest("multiassign07"),
            new UnLuaCTest("multiassign08"),
            new UnLuaCTest("multiassign09"),
            new UnLuaCTest("multiassign10"),
            new UnLuaCTest("multiassign11"),
            new UnLuaCTest("multiassign12"),
            new UnLuaCTest("expression"),
            new UnLuaCTest("expression02"),
            new UnLuaCTest("functioncall"),
            new UnLuaCTest("self01"),
            new UnLuaCTest("literallist"),
            new UnLuaCTest("multiliteraltarget"),
            new UnLuaCTest("closure"),
            new UnLuaCTest("ellipsis03"),
            new UnLuaCTest("ifthen"),
            new UnLuaCTest("condition"),
            new UnLuaCTest("condition02"),
            new UnLuaCTest("condition03"),
            new UnLuaCTest("condition04"),
            new UnLuaCTest("nestedif"),
            new UnLuaCTest("nestedif02"),
            new UnLuaCTest("ifthenelse"),
            new UnLuaCTest("while"),
            new UnLuaCTest("while02"),
            new UnLuaCTest("while03"),
            new UnLuaCTest("while04"),
            new UnLuaCTest("while05"),
            new UnLuaCTest("while06"),
            new UnLuaCTest("while07"),
            new UnLuaCTest("while08"),
            new UnLuaCTest("repeat"),
            new UnLuaCTest("repeat02"),
            new UnLuaCTest("repeat03"),
            new UnLuaCTest("if01"),
            new UnLuaCTest("if02"),
            new UnLuaCTest("if03"),
            new UnLuaCTest("if04"),
            new UnLuaCTest("if05"),
            new UnLuaCTest("if06"),
            new UnLuaCTest("if07"),
            new UnLuaCTest("else01"),
            new UnLuaCTest("else02"),
            new UnLuaCTest("else03"),
            new UnLuaCTest("else04"),
            new UnLuaCTest("else05"),
            new UnLuaCTest("else06"),
            new UnLuaCTest("else07"),
            new UnLuaCTest("else08"),
            new UnLuaCTest("booleanassign01"),
            new UnLuaCTest("booleanassign02"),
            new UnLuaCTest("booleanassign03"),
            new UnLuaCTest("booleanassign04"),
            new UnLuaCTest("booleanassign05"),
            new UnLuaCTest("booleanassign06"),
            new UnLuaCTest("booleanassign07"),
            new UnLuaCTest("booleanassign08"),
            new UnLuaCTest("booleanassign09"),
            new UnLuaCTest("booleanassign10"),
            new UnLuaCTest("booleanassign11"),
            new UnLuaCTest("booleanassign12"),
            new UnLuaCTest("booleanassign13"),
            new UnLuaCTest("booleanassign14"),
            new UnLuaCTest("booleanassign15"),
            new UnLuaCTest("booleanassign16"),
            new UnLuaCTest("booleanassign17"),
            new UnLuaCTest("booleanassign18"),
            new UnLuaCTest("booleanassign19"),
            new UnLuaCTest("booleanassign20"),
            new UnLuaCTest("booleanassign21"),
            new UnLuaCTest("booleanassign22"),
            new UnLuaCTest("booleanassign23"),
            new UnLuaCTest("booleanassign24"),
            new UnLuaCTest("booleanassign25"),
            new UnLuaCTest("booleanassign26"),
            new UnLuaCTest("booleanassign27"),
            new UnLuaCTest("booleanassign28"),
            new UnLuaCTest("booleanassign29"),
            new UnLuaCTest("booleanselfassign01"),
            new UnLuaCTest("booleanexpression01"),
            new UnLuaCTest("booleanexpression02"),
            new UnLuaCTest("booleanexpression03"),
            new UnLuaCTest("booleanexpression04"),
            new UnLuaCTest("booleanexpression05"),
            new UnLuaCTest("booleanexpression06"),
            new UnLuaCTest("booleanexpression07"),
            new UnLuaCTest("booleanexpression08"),
            new UnLuaCTest("booleanexpression09"),
            new UnLuaCTest("booleanexpression10"),
            new UnLuaCTest("booleanexpression11"),
            new UnLuaCTest("booleanmultiassign01"),
            new UnLuaCTest("booleanmultiassign02"),
            new UnLuaCTest("compareassign01"),
            new UnLuaCTest("compareassign02"),
            new UnLuaCTest("compareexpression"),
            new UnLuaCTest("compareexpression02"),
            new UnLuaCTest("combinebexpression01"),
            new UnLuaCTest("combinebexpression02"),
            new UnLuaCTest("combinebexpression03"),
            new UnLuaCTest("combinebexpression04"),
            new UnLuaCTest("combinebexpression05"),
            new UnLuaCTest("combinebexpression06"),
            new UnLuaCTest("combinebexpression07"),
            new UnLuaCTest("combinebassign01"),
            new UnLuaCTest("combinebassign02"),
            new UnLuaCTest("combinebassign03"),
            new UnLuaCTest("combinebassign04", UnLuaCTest.DEFAULT_VERSION, UnLuaCTest.RELAXED_SCOPE),
            new UnLuaCTest("combinebassign05"),
            new UnLuaCTest("combinebassign07"),
            new UnLuaCTest("complexassign01"),
            new UnLuaCTest("complexassign02"),
            new UnLuaCTest("complexassign03"),
            new UnLuaCTest("compare01"),
            new UnLuaCTest("compareorder01"),
            new UnLuaCTest("compareorder02"),
            new UnLuaCTest("compareorder03"),
            new UnLuaCTest("compareorder04"),
            new UnLuaCTest("compareorder05"),
            new UnLuaCTest("compareorder06"),
            new UnLuaCTest("compareorder07"),
            new UnLuaCTest("compareorder08"),
            new UnLuaCTest("table01"),
            new UnLuaCTest("table02"),
            new UnLuaCTest("table03"),
            new UnLuaCTest("table06"),
            new UnLuaCTest("table07"),
            new UnLuaCTest("table08"),
            new UnLuaCTest("localfunction01"),
            new UnLuaCTest("localfunction02"),
            new UnLuaCTest("localfunction03"),
            new UnLuaCTest("localfunction04"),
            new UnLuaCTest("declare"),
            new UnLuaCTest("declare02"),
            new UnLuaCTest("declare03"),
            new UnLuaCTest("declare04"),
            new UnLuaCTest("declare05"),
            new UnLuaCTest("adjust01"),
            new UnLuaCTest("adjust04"),
            new UnLuaCTest("adjust05"),
            new UnLuaCTest("adjust06"),
            new UnLuaCTest("final01"),
            new UnLuaCTest("final02"),
            new UnLuaCTest("doend01", UnLuaCTest.DEFAULT_VERSION, UnLuaCTest.RELAXED_SCOPE),
            new UnLuaCTest("doend02"),
            new UnLuaCTest("doend03"),
            new UnLuaCTest("doend04"),
            new UnLuaCTest("doend05"),
            new UnLuaCTest("doend06"),
            new UnLuaCTest("doend07", UnLuaCTest.DEFAULT_VERSION, UnLuaCTest.RELAXED_SCOPE),
            new UnLuaCTest("doend08", UnLuaCTest.DEFAULT_VERSION, UnLuaCTest.RELAXED_SCOPE),
            new UnLuaCTest("control01"),
            new UnLuaCTest("control02"),
            new UnLuaCTest("control03"),
            new UnLuaCTest("control04"),
            new UnLuaCTest("control05"),
            new UnLuaCTest("control06"),
            new UnLuaCTest("loop01"),
            new UnLuaCTest("loop02"),
            new UnLuaCTest("loop03"),
            new UnLuaCTest("loop04"),
            new UnLuaCTest("method01"),
            new UnLuaCTest("method02"),
            new UnLuaCTest("inlinefunction01"),
            new UnLuaCTest("inlinefunction02"),
            new UnLuaCTest("inlineconstant01"),
            new UnLuaCTest("string01"),
            new UnLuaCTest("string02"),
            new UnLuaCTest("string04"),
            new UnLuaCTest("string05"),
            new UnLuaCTest("upvalue01"),
            new UnLuaCTest("upvalue02"),
            new UnLuaCTest("upvalue03"),
            new UnLuaCTest("upvalue04"),
            new UnLuaCTest("upvalue05"),
            new UnLuaCTest("upvalue06", UnLuaCTest.DEFAULT_VERSION, UnLuaCTest.RELAXED_SCOPE),
            new UnLuaCTest("break01"),
            new UnLuaCTest("break02"),
            new UnLuaCTest("break03"),
            new UnLuaCTest("break04"),
            new UnLuaCTest("break05"),
            new UnLuaCTest("break06"),
            new UnLuaCTest("break07"),
            new UnLuaCTest("break08"),
            new UnLuaCTest("break09"),
            new UnLuaCTest("break10"),
            new UnLuaCTest("break11"),
            new UnLuaCTest("break12"),
            new UnLuaCTest("break13"),
            new UnLuaCTest("break14"),
            new UnLuaCTest("break15"),
            new UnLuaCTest("break16"),
            new UnLuaCTest("break17"),
            new UnLuaCTest("break19"),
            new UnLuaCTest("break20"),
            new UnLuaCTest("break21"),
            new UnLuaCTest("break22", UnLuaCTest.DEFAULT_VERSION, UnLuaCTest.RELAXED_SCOPE), //TODO: relaxed scope is maybe not necessary if scopes inform hanger resolution?
            new UnLuaCTest("break23", UnLuaCTest.DEFAULT_VERSION, UnLuaCTest.RELAXED_SCOPE), //TODO: relaxed scope is maybe not necessary if scopes inform hanger resolution?
            new UnLuaCTest("break24"),
            new UnLuaCTest("close01"),
            new UnLuaCTest("close02"),
            new UnLuaCTest("close03"),
            new UnLuaCTest("close04"),
            new UnLuaCTest("close05"),
            new UnLuaCTest("close06"),
            new UnLuaCTest("close07"),
            new UnLuaCTest("close08"),
            new UnLuaCTest("always01"),
            new UnLuaCTest("always02"),
            new UnLuaCTest("always03"),
            new UnLuaCTest("always04"),
            new UnLuaCTest("once01", UnLuaCTest.DEFAULT_VERSION, UnLuaCTest.RELAXED_SCOPE), //TODO: maybe should enforce not using goto here?
            new UnLuaCTest("once02"),
            new UnLuaCTest("once03"),
            new UnLuaCTest("once04"),
            new UnLuaCTest("once05"),
            new UnLuaCTest("unused01"),
            new UnLuaCTest("report01a"),
            new UnLuaCTest("report01b"),
            new UnLuaCTest("report01c"),
            new UnLuaCTest("report01d"),
            new UnLuaCTest("report01_full"),
            new UnLuaCTest("report02"),
            new UnLuaCTest("report02a"),
            new UnLuaCTest("report02b"),
            new UnLuaCTest("report02c"),
            new UnLuaCTest("report02d"),
            new UnLuaCTest("report02e"),
            new UnLuaCTest("report03"),
            new UnLuaCTest("report04"),
            new UnLuaCTest("report05"),
            new UnLuaCTest("report06"),
            new UnLuaCTest("scope02"),
            new UnLuaCTest("scope03"),
            new UnLuaCTest("51_expression"),
            new UnLuaCTest("51_expression2"),
            new UnLuaCTest("51_expression03"),
            new UnLuaCTest("51_string03"),
            new UnLuaCTest("51_ellipsis"),
            new UnLuaCTest("51_ellipsis02"),
            new UnLuaCTest("51_adjust02"),
            new UnLuaCTest("51_adjust03"),
            new UnLuaCTest("51_method03"),
            new UnLuaCTest("52_loadkx01"),
            new UnLuaCTest("52_goto01"),
            new UnLuaCTest("52_goto02"),
            new UnLuaCTest("52_goto03"),
            new UnLuaCTest("52_goto04"),
            new UnLuaCTest("52_goto05"),
            new UnLuaCTest("52_goto06"),
            new UnLuaCTest("52_goto08"),
            new UnLuaCTest("53_expression"),
            new UnLuaCTest("53_expression02"),
            new UnLuaCTest("54_tbc01")
    );
    private static final TestExecutor TEST_EXECUTOR = new TestExecutor(TESTS);

    @Test
    public void runTests() throws IOException {
        System.out.println("Running tests");
        var spec = new LuaSpec(0x54);
        var uspec = new UnLuaCSpec();
        //uspec.disassemble = true;
        var config = new Configuration();
        config.strict_scope = true;

        TEST_EXECUTOR.run(spec, uspec, config);
    }
}
