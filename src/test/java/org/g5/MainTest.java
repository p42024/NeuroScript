package org.g5;

import org.antlr.v4.runtime.tree.ParseTree;
import org.g5.exceptions.*;
import org.g5.typecheck.ScopedTypeTable;
import org.g5.typecheck.types.BooleanExpression;
import org.g5.typecheck.types.NumberExpression;
import org.g5.typecheck.types.model.*;
import org.g5.typecheck.types.training.*;

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.IOException;

public class MainTest {
    private boolean testCode(String code) {
        try {
            Main.parseAndCompileString(code);
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
            return false;
        }
        return true;
    }

    @Test
    public void testLayerSizeSuccess() {
        assert (testCode("hidden = linear(512, 300); model_test_1 = sequential(linear(28 * 28, 512) -> hidden -> Sigmoid -> linear(300, 20)); size = 512; model_test_1 = sequential(linear(10, size) -> hidden -> Sigmoid -> linear(300, 512));"));
    }

    @Test
    public void testLayerSizeFail() {
        assertThrows(BaseException.class, () -> Main.parseAndCompileString("hidden = linear(512, 300); model_test_1 = sequential(linear(28 * 28, 10) -> hidden -> Sigmoid -> linear(300, 20));"));
    }

    @Test
    public void testLayerPositive() {
        assert (testCode("hidden = linear(1, 100000);"));
        assert (testCode("hidden = linear(10 + 5, 30 * 2);"));

        assertThrows(WrongArgumentException.class, () -> Main.parseAndCompileString("hidden = linear(-512, 300);"));
        assertThrows(WrongArgumentException.class, () -> Main.parseAndCompileString("hidden = linear(512, -300);"));
        assertThrows(WrongArgumentException.class, () -> Main.parseAndCompileString("hidden = linear(100 * (0 - 1), -300);"));
        assertThrows(WrongArgumentException.class, () -> Main.parseAndCompileString("hidden = linear(100 - 101, 300);"));
    }

    @Test
    public void testLayerInteger() {
        assert (testCode("hidden = linear(10, 30);"));
        assert (testCode("hidden = linear(10 - 5, 30 * 2);"));

        assertThrows(WrongTypeException.class, () -> Main.parseAndCompileString("hidden = linear(1/512, 300);"));
        assertThrows(WrongTypeException.class, () -> Main.parseAndCompileString("hidden = linear(0.512, 300);"));
        assertThrows(WrongTypeException.class, () -> Main.parseAndCompileString("hidden = linear(0.2, 2.5);"));
    }

    @Test
    public void testBreakSuccess() {
        assert (testCode("test=10; loop{ if(test==10){ break; } break; }"));
    }

    @Test
    public void testBreakSuccessTrain() {
        assert (testCode("test=10; train { loop{ if(test==10){ break; } break; } }"));
    }

    @Test
    public void testBreakFail() {
        assertThrows(ScopeNotBreakableException.class, () -> Main.parseAndCompileString("test = 20; break;"));
    }

    @Test
    public void testBreakFailTrain() {
        assertThrows(ScopeNotBreakableException.class, () -> Main.parseAndCompileString("test = 20; train { break; }"));
    }

    @Test
    public void testTypeCheckString() throws IOException {
        ParseTree tree = TestUtil.getParseTreeFromString("""
                a = " asd asd asd asd asd as
                        asd";
                               \s
                b = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut id volutpat elit, pharetra vestibulum tortor. Curabitur non pellentesque tortor. Aliquam erat volutpat. Vivamus molestie, dolor vitae pretium suscipit, tellus nunc mollis ligula, egestas consequat augue erat non odio. Aenean vel est lectus. Curabitur ante ante, mollis ac sodales vitae, efficitur id eros. Pellentesque quis magna at elit mollis egestas sit amet vel tellus.
                               \s
                \\"
                               \s
                Phasellus tempus pretium augue. Proin tincidunt viverra molestie. Nulla vel libero ut felis posuere congue in at massa. Pellentesque dui leo, viverra quis luctus eget, laoreet sit amet massa. Proin eget nisl lectus. Vestibulum ut turpis elit. Nullam aliquet tincidunt aliquam. Cras vehicula, mauris vitae aliquam volutpat, sem sem efficitur lacus, mollis cursus massa lectus nec neque. Curabitur nibh neque, feugiat vitae lacinia id, pellentesque ac massa. Ut varius tristique nibh at vulputate. Nulla ut ipsum lorem. Donec ac est mauris.
                               \s
                               \s
                               \s
                Vestibulum eu nisi semper, lobortis nibh vel, tincidunt ante. Ut nunc urna, egestas quis facilisis eget, maximus quis ligula. Suspendisse fermentum sagittis tortor ac accumsan. Sed lorem ex, venenatis vitae lectus at, maximus convallis felis. Fusce et felis iaculis, molestie tortor at, lobortis libero. Nunc convallis ac sem vel pretium. Curabitur tincidunt orci ac pellentesque rhoncus. Donec eu augue ac urna ornare pharetra. Ut rhoncus gravida nunc, et sagittis nunc laoreet ut. Etiam pellentesque tortor in diam venenatis, quis hendrerit ante condimentum. Quisque interdum diam non dolor sagittis mattis. Curabitur at magna sed nisl interdum ornare. In turpis nisi, porta quis massa in, tincidunt pretium nisl. Aliquam a volutpat nisi. Ut egestas, tortor vel rhoncus mollis, magna magna vulputate nisl, et lacinia libero metus a enim.
                               \s
                               \s
                               \s
                Ut vestibulum arcu leo, eget convallis eros fringilla eu. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Nunc sit amet dolor faucibus, imperdiet libero eget, gravida enim. Pellentesque fringilla mattis nulla quis rhoncus. Maecenas egestas eleifend purus, vitae mattis magna semper ut. Vivamus a hendrerit metus. Sed orci tellus, facilisis et posuere at, auctor ac arcu. Nunc fermentum scelerisque vulputate. Proin ut metus eu orci pellentesque bibendum. Mauris sapien quam, iaculis eu enim quis, elementum tincidunt libero. Donec varius quis mi sit amet volutpat. Praesent sollicitudin, velit sit amet euismod feugiat, lorem neque malesuada urna, quis iaculis massa risus a nulla. Sed vitae est quis purus sollicitudin fringilla.
                               \s
                               \s
                               \s
                Donec in finibus ligula. Nullam at turpis neque. Phasellus dapibus ante non nulla commodo ultricies. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nam sollicitudin augue quis suscipit ultrices. Cras sed sapien sit amet massa facilisis euismod in ut quam. Aliquam at sapien vulputate, fermentum elit iaculis, hendrerit magna. Vestibulum egestas eros vitae nulla congue, ut hendrerit mauris porttitor. Nulla auctor lectus turpis, sed aliquam nibh imperdiet et. Sed in ex maximus, venenatis justo in, laoreet neque.";""");
        ScopedTypeTable scopedTypeTable = TestUtil.getTypeTable(tree);

        TestUtil.traverseAndCheck(tree, scopedTypeTable, String.class);
    }

    @Test
    public void testTypeCheckFloat() throws IOException {
        ParseTree tree = TestUtil.getParseTreeFromString("a = 1.35409430939; b = 6.0;");
        ScopedTypeTable scopedTypeTable = TestUtil.getTypeTable(tree);

        TestUtil.traverseAndCheck(tree, scopedTypeTable, NumberExpression.class, (a, b, evaluatedExpression) -> !evaluatedExpression.isInt());
    }

    @Test
    public void testTypeCheckFloatToInt() {
        assertThrows(AssignmentTypeException.class, () -> Main.parseAndCompileString("a = 1.35409430939; a = 1;"));
    }

    @Test
    public void testTypeCheckFloatArithmeticAddition() throws IOException {
        ParseTree tree = TestUtil.getParseTreeFromString("a = 1.0 + 2.0; b = 1.5 + 1.2;");
        ScopedTypeTable scopedTypeTable = TestUtil.getTypeTable(tree);

        TestUtil.traverseAndCheck(tree, scopedTypeTable, NumberExpression.class, (a, b, evaluatedExpression) -> !evaluatedExpression.isInt());
    }

    @Test
    public void testTypeCheckFloatArithmeticSubtraction() throws IOException {
        ParseTree tree = TestUtil.getParseTreeFromString("a = 1.0 - 2.0; b = 1.5 - 1.2;");
        ScopedTypeTable scopedTypeTable = TestUtil.getTypeTable(tree);

        TestUtil.traverseAndCheck(tree, scopedTypeTable, NumberExpression.class, (a, b, evaluatedExpression) -> !evaluatedExpression.isInt());
    }

    @Test
    public void testTypeCheckFloatArithmeticMultiplication() throws IOException {
        ParseTree tree = TestUtil.getParseTreeFromString("a = 1.0 * 2.0; b = 1.5 * 1.2;");
        ScopedTypeTable scopedTypeTable = TestUtil.getTypeTable(tree);

        TestUtil.traverseAndCheck(tree, scopedTypeTable, NumberExpression.class, (a, b, evaluatedExpression) -> !evaluatedExpression.isInt());
    }

    @Test
    public void testTypeCheckFloatArithmeticDivision() throws IOException {
        ParseTree tree = TestUtil.getParseTreeFromString("a = 1.0 / 2.0; b = 1.5 / 1.2;");
        ScopedTypeTable scopedTypeTable = TestUtil.getTypeTable(tree);

        TestUtil.traverseAndCheck(tree, scopedTypeTable, NumberExpression.class, (a, b, evaluatedExpression) -> !evaluatedExpression.isInt());
    }

    @Test
    public void testTypeCheckFloatArithmetic() throws IOException {
        ParseTree tree = TestUtil.getParseTreeFromString("a = 1.0 / 2.0 + 10.0 - 100 * (10.0 + 10); b = ((1.0 / 2.0) + (10.0 - 100) * (10.0 + 10));");
        ScopedTypeTable scopedTypeTable = TestUtil.getTypeTable(tree);

        TestUtil.traverseAndCheck(tree, scopedTypeTable, NumberExpression.class, (a, b, evaluatedExpression) -> !evaluatedExpression.isInt());
    }


    @Test
    public void testTypeCheckInt() throws IOException {
        ParseTree tree = TestUtil.getParseTreeFromString("a = 10; b = 20;");
        ScopedTypeTable scopedTypeTable = TestUtil.getTypeTable(tree);

        TestUtil.traverseAndCheck(tree, scopedTypeTable, NumberExpression.class, (a, b, evaluatedExpression) -> evaluatedExpression.isInt());
    }

    @Test
    public void testTypeCheckIntToFloat() {
        assertThrows(AssignmentTypeException.class, () -> Main.parseAndCompileString("a = 1; a = 1.35409430939;"));
    }

    @Test
    public void testTypeCheckIntArithmeticAddition() throws IOException {
        ParseTree tree = TestUtil.getParseTreeFromString("a = 1 + 2; b = 1 + 2 + 3;");
        ScopedTypeTable scopedTypeTable = TestUtil.getTypeTable(tree);

        TestUtil.traverseAndCheck(tree, scopedTypeTable, NumberExpression.class, (a, b, evaluatedExpression) -> evaluatedExpression.isInt());
    }

    @Test
    public void testTypeCheckIntArithmeticSubtraction() throws IOException {
        ParseTree tree = TestUtil.getParseTreeFromString("a = 1 - 2; b = 1 - 2 - 3;");
        ScopedTypeTable scopedTypeTable = TestUtil.getTypeTable(tree);

        TestUtil.traverseAndCheck(tree, scopedTypeTable, NumberExpression.class, (a, b, evaluatedExpression) -> evaluatedExpression.isInt());
    }

    @Test
    public void testTypeCheckIntArithmeticMultiplication() throws IOException {
        ParseTree tree = TestUtil.getParseTreeFromString("a = 1 * 2; b = 1 * 2 * 3;");
        ScopedTypeTable scopedTypeTable = TestUtil.getTypeTable(tree);

        TestUtil.traverseAndCheck(tree, scopedTypeTable, NumberExpression.class, (a, b, evaluatedExpression) -> evaluatedExpression.isInt());
    }

    @Test
    public void testTypeCheckIntArithmeticDivision() throws IOException {
        ParseTree tree = TestUtil.getParseTreeFromString("a = 1 / 2; b = 1 / 2 / 3;");
        ScopedTypeTable scopedTypeTable = TestUtil.getTypeTable(tree);

        TestUtil.traverseAndCheck(tree, scopedTypeTable, NumberExpression.class, (a, b, evaluatedExpression) -> !evaluatedExpression.isInt());
    }

    @Test
    public void testTypeCheckIntArithmetic() throws IOException {
        ParseTree tree = TestUtil.getParseTreeFromString("a = 10 - 100 * (10 + 10); b = (10 - (100 * (10 + 10)));");
        ScopedTypeTable scopedTypeTable = TestUtil.getTypeTable(tree);

        TestUtil.traverseAndCheck(tree, scopedTypeTable, NumberExpression.class, (a, b, evaluatedExpression) -> evaluatedExpression.isInt());
    }

    @Test
    public void testTypeCheckIntArithmetic2() throws IOException {
        ParseTree tree = TestUtil.getParseTreeFromString("a = 10 - 100 * (10 + 10) + (1/2); b = (10 - (100 * (10 + 10))) + (10 + 1/2);");
        ScopedTypeTable scopedTypeTable = TestUtil.getTypeTable(tree);

        TestUtil.traverseAndCheck(tree, scopedTypeTable, NumberExpression.class, (a, b, evaluatedExpression) -> !evaluatedExpression.isInt());
    }

    @Test
    public void testTypeCheckBool() throws IOException {
        ParseTree tree = TestUtil.getParseTreeFromString("a = 1 < 2 and 2 < 1; b = false or true; ");
        ScopedTypeTable scopedTypeTable = TestUtil.getTypeTable(tree);

        TestUtil.traverseAndCheck(tree, scopedTypeTable, BooleanExpression.class);
    }

    @Test
    public void testTypeCheckActivation() throws IOException {
        ParseTree tree = TestUtil.getParseTreeFromString("a = ReLU; b = Sigmoid;");
        ScopedTypeTable scopedTypeTable = TestUtil.getTypeTable(tree);

        TestUtil.traverseAndCheck(tree, scopedTypeTable, Activation.class);
    }

    @Test
    public void testTypeCheckLinearLayer() throws IOException {
        ParseTree tree = TestUtil.getParseTreeFromString("a = linear(126, 512); b = linear(512, 126);");
        ScopedTypeTable scopedTypeTable = TestUtil.getTypeTable(tree);

        TestUtil.traverseAndCheck(tree, scopedTypeTable, LinearLayer.class);
    }

    @Test
    public void testTypeCheckSequentialModel() throws IOException {
        ParseTree tree = TestUtil.getParseTreeFromString("a = sequential(linear(128, 128) -> ReLU -> linear(128, 52)); b = sequential(sequential(linear(128, 128) -> ReLU -> linear(128, 52) -> ReLU -> linear(52, 128)) -> ReLU -> linear(128, 52) -> ReLU -> linear(52, 10));");
        ScopedTypeTable scopedTypeTable = TestUtil.getTypeTable(tree);

        TestUtil.traverseAndCheck(tree, scopedTypeTable, SequentialModel.class);
    }


    @Test
    public void testTypeCheckNumberArray() throws IOException {
        ParseTree tree = TestUtil.getParseTreeFromString("a = [1.2, 2.5, 3/3]; b = [2/3, 1.2 * 5, 2.0, 2 * 2 - 0.5];");
        ScopedTypeTable scopedTypeTable = TestUtil.getTypeTable(tree);

        TestUtil.traverseAndCheck(tree, scopedTypeTable, NumberArray.class);
    }

    @Test
    public void testTypeCheckPrediction() throws IOException {
        ParseTree tree = TestUtil.getParseTreeFromString("sequential_model = sequential(linear(784, 10)); train { import data \"MNISTDigits\" as MNISTDigits; train_prediction = sequential_model(MNISTDigits); }");
        ScopedTypeTable scopedTypeTable = TestUtil.getTypeTable(tree);

        TestUtil.traverseAndCheckAllScopes(tree, scopedTypeTable, new String[]{"train_prediction"}, Data.class);
    }

    @Test
    public void testTypeCheckDataImport() throws IOException {
        ParseTree tree = TestUtil.getParseTreeFromString("train { import data \"MNISTDigits\" as training_data; import data \"MNISTDigitsTest\" as test_data; }\n");
        ScopedTypeTable scopedTypeTable = TestUtil.getTypeTable(tree);

        TestUtil.traverseAndCheckAllScopes(tree, scopedTypeTable, new String[]{"training_data", "test_data"}, Data.class);
    }

    @Test
    public void testCustomData() throws IOException {
        ParseTree tree = TestUtil.getParseTreeFromString("train { import data \"test_data\" as training_data; import data \"test_test_data\" as test_data; }\n");
        ScopedTypeTable scopedTypeTable = TestUtil.getTypeTable(tree);

        TestUtil.traverseAndCheckAllScopes(tree, scopedTypeTable, new String[]{"training_data", "test_data"}, Data.class);
    }

    @Test
    public void testTypeCheckLoss() throws IOException {
        ParseTree tree = TestUtil.getParseTreeFromString("sequential_model = sequential(linear(784, 10)); train { import data \"MNISTDigitsTest\" as test_data; test_prediction = sequential_model(test_data); loss = MSE(test_prediction, test_data); }");
        ScopedTypeTable scopedTypeTable = TestUtil.getTypeTable(tree);

        TestUtil.traverseAndCheckAllScopes(tree, scopedTypeTable, new String[]{"loss"}, Loss.class);
    }

    @Test
    public void testTypeCheckCalculateAccuracy() throws IOException {
        ParseTree tree = TestUtil.getParseTreeFromString("sequential_model = sequential(linear(784, 10)); train { import data \"MNISTDigitsTest\" as test_data; test_prediction = sequential_model(test_data); accuracy = calculate_accuracy(test_prediction, test_data); }");
        ScopedTypeTable scopedTypeTable = TestUtil.getTypeTable(tree);

        TestUtil.traverseAndCheckAllScopes(tree, scopedTypeTable, new String[]{"accuracy"}, CalculateAccuracy.class);
    }
}
