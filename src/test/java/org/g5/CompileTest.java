package org.g5;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

public class CompileTest {
    private static final String import_code = "import torch\n" +
            "import pandas as pd\n" +
            "from torch import nn\n" +
            "from torch.utils.data import DataLoader, Dataset\n" +
            "from torchvision import datasets\n" +
            "from torchvision.transforms import ToTensor\n" +
            "import os\n";


    /*==================================================================*
     *                             Utility                              *
     *==================================================================*/

    private boolean testCompileDefine(String code, String expected) {
        String result = Main.parseAndCompileString(code);
        // Remove unnecessary formatting
        result = result.replaceAll("\s", " ").replaceAll("(( )+\n)", "").replaceAll("\n\n", "\n");
        assertEquals(import_code + expected + "\n", result);
        return true;
    }

    private boolean testCompileTrain(String code, String expected) {
        return testCompileTrain(null, code, null, expected);
    }

    private boolean testCompileTrain(String defineCode, String code, String defineExpected, String expected) {
        code = "train { " + code + " }";
        if (defineCode != null) {
            code = defineCode + "\n" + code;
        }

        expected = """
                def train_0():
                """ +
                "    " + expected.replaceAll("\n", "\n    ") +
                """
                                
                        train_0()""";
        if (defineExpected != null) {
            expected = defineExpected + "\n" + expected;
        }
        String result = Main.parseAndCompileString(code);
        // Remove unnecessary formatting
        result = result.replaceAll("\s", " ") // Ensure correct types of whitespace
                .replaceAll("(( )+\n)|((?<![^\n])\n)", ""); // Remove empty lines
        assertEquals(import_code + expected + "\n", result);
        return true;
    }

    /*==================================================================*
     *                            Statements                            *
     *==================================================================*/

    /* ============== Assignment ==============*/
    @Test
    public void testDefineAssignment() {
        String code = "a = 1;";
        String expected = "a = 1";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testTrainAssignment() {
        String code = "a = 1;";
        String expected = "a = 1";
        assert (testCompileTrain(code, expected));
    }

    /* ============== Loop ==============*/

    @Test
    public void testDefineLoop() {
        String code = "loop { a = 1; }";
        String expected = """
                               
                while True:
                    a = 1""";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testTrainLoop() {
        String code = "loop { a = 1; }";
        String expected = """
                while True:
                    a = 1""";
        assert (testCompileTrain(code, expected));
    }

    @Test
    public void testDefineBreakLoop() {
        String code = "loop { break; }";
        String expected = """
                                
                while True:
                    break""";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testTrainBreakLoop() {
        String code = "loop { break; }";
        String expected = """
                while True:
                    break""";
        assert (testCompileTrain(code, expected));
    }

    /* ============== If statement ==============*/

    @Test
    public void testDefineIf() {
        String code = "if 1 == 1 { a = 1; }";
        String expected = """     
                if (1 == 1):
                    a = 1""";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testTrainIf() {
        String code = "if 1 == 1 { a = 1; }";
        String expected = """     
                if (1 == 1):
                    a = 1""";
        assert (testCompileTrain(code, expected));
    }

    @Test
    public void testDefineIfElse() {
        String code = "if 1 == 1 { a = 1; } else { a = 2; }";
        String expected = """     
                if (1 == 1):
                    a = 1
                else:
                    a = 2""";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testTrainIfElse() {
        String code = "if 1 == 1 { a = 1; } else { a = 2; }";
        String expected = """     
                if (1 == 1):
                    a = 1
                else:
                    a = 2""";
        assert (testCompileTrain(code, expected));
    }

    /* ============== Export model ==============*/
    @Test
    public void testDefineExportString() {
        String code = "test = sequential(linear(10, 10)); export model test as \"test\";";
        String expected = """     
                test = nn.Sequential(nn.Linear(10, 10))
                torch.save(test, os.getcwd() + '\\\\test.pt')""";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testTrainExportString() {
        String defineCode = "test = sequential(linear(10, 10));";
        String code = "export model test as \"test\";";
        String defineExpected = "test = nn.Sequential(nn.Linear(10, 10))";
        String expected = """     
                torch.save(test, os.getcwd() + '\\\\test.pt')""";
        assert (testCompileTrain(defineCode, code, defineExpected, expected));
    }

    @Test
    public void testDefineExportId() {
        String code = "test = sequential(linear(10, 10)); name = \"test\"; export model test as name;";
        String expected = """     
                test = nn.Sequential(nn.Linear(10, 10))
                name = "test"
                torch.save(test, os.getcwd() + '\\\\test.pt')""";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testTrainExportId() {
        String defineCode = "test = sequential(linear(10, 10));";
        String code = "name = \"test\"; export model test as name;";
        String defineExpected = "test = nn.Sequential(nn.Linear(10, 10))";
        String expected = """
                name = "test"     
                torch.save(test, os.getcwd() + '\\\\test.pt')""";
        assert (testCompileTrain(defineCode, code, defineExpected, expected));
    }

    @Test
    public void testTrainExportIdInDefine() {
        String defineCode = "test = sequential(linear(10, 10)); name = \"test\";";
        String code = "export model test as name;";
        String defineExpected = """
                test = nn.Sequential(nn.Linear(10, 10))
                name = \"test\"""";
        String expected = """     
                torch.save(test, os.getcwd() + '\\\\test.pt')""";
        assert (testCompileTrain(defineCode, code, defineExpected, expected));
    }


    /* ============== Print ==============*/
    @Test
    public void testDefinePrintString() {
        String code = "print(\"test\");";
        String expected = """     
                print(\"test\")""";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testTrainPrintString() {
        String code = "print(\"test\");";
        String expected = """     
                print(\"test\")""";
        assert (testCompileTrain(code, expected));
    }

    @Test
    public void testDefinePrintId() {
        String code = "str = \"test\"; print(str);";
        String expected = """     
                str = "test"
                print(str)""";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testTrainPrintId() {
        String code = "str = \"test\"; print(str);";
        String expected = """     
                str = "test"
                print(str)""";
        assert (testCompileTrain(code, expected));
    }

    @Test
    public void testTrainPrintIdInDefine() {
        String code = "print(str);";
        String expected = """     
                print(str)""";
        String defineCode = "str = \"test\";";
        String defineExpected = """
                str = \"test\"""";
        assert (testCompileTrain(defineCode, code, defineExpected, expected));
    }

    /* ============== Data import ==============*/

    @Test
    public void testTrainImportData() {
        // 'import' 'data' stringOrId 'as' id  ';'
        String code = "train { import data \"test.csv\" as test; }";
        String expected = """     
                class CustomDataset(Dataset):
                    def __init__(self, csv_file, target_column):
                        self.data_frame = pd.read_csv(csv_file)
                        self.target_column = target_column
                    def __len__(self):
                        return len(self.data_frame)
                    def __getitem__(self, idx):
                        if torch.is_tensor(idx):
                            idx = idx.tolist()
                        sample = self.data_frame.iloc[idx, :]
                        target = sample[self.target_column]
                        sample = sample.drop(labels=[self.target_column])
                        sample = torch.tensor(sample.values, dtype=torch.float32)
                        target = torch.tensor(target, dtype=torch.long)
                        return sample, target
                def train_0():
                    test_dataset = CustomDataset(csv_file='test.csv', target_column='target')
                    test = DataLoader(test_dataset, batch_size=16, shuffle=True)
                                
                train_0()""";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testTrainImportDataMNISTDigits() {
        String code = "import data \"MNISTDigits\" as digits;";
        String expected = """     
                MNISTDigitsDataset = datasets.MNIST(root="data", train=True, download=True, transform=ToTensor())
                MNISTDigits = DataLoader(MNISTDigitsDataset, batch_size=16)""";
        assert (testCompileTrain(code, expected));
    }

    @Test
    public void testTrainImportDataMNISTDigitsTest() {
        String code = "import data \"MNISTDigitsTest\" as tests;";
        String expected = """     
                MNISTDigitsTestDataset = datasets.MNIST(root="data", train=False, download=True, transform=ToTensor())
                MNISTDigitsTest = DataLoader(MNISTDigitsTestDataset, batch_size=16)""";
        assert (testCompileTrain(code, expected));
    }

    /* ============== Model imports ==============*/
    @Test
    public void testTrainImportModelString() {
        String code = "import model \"test.pt\" as test;";
        String expected = """     
                test = torch.load('test.pt')
                test.eval()""";
        assert (testCompileTrain(code, expected));
    }

    @Test
    public void testTrainImportModelId() {
        String code = "path = \"test.pt\"; import model path as test;";
        String expected = """     
                path = "test.pt"
                test = torch.load('test.pt')
                test.eval()""";
        assert (testCompileTrain(code, expected));
    }

    @Test
    public void testTrainImportModelDefineId() {
        String defineCode = "path = \"test.pt\";";
        String code = "import model path as test;";
        String defineExpected = "path = \"test.pt\"";
        String expected = """     
                test = torch.load('test.pt')
                test.eval()""";
        assert (testCompileTrain(defineCode, code, defineExpected, expected));
    }

    /* ============== SGD ==============*/
    @Test
    public void testTrainSGDDataImport() {
        String code = "sequential_model = sequential(linear(784, 10)); train { import data \"MNISTDigits\" as MNISTDigits; train_prediction = sequential_model(MNISTDigits); loss = CE(train_prediction, MNISTDigits); SGD(sequential_model, loss, 0.01);}";
        String expected = """     
                sequential_model = nn.Sequential(nn.Linear(784, 10))
                def train_0(sequential_model):
                    CrossEntropy_0 = nn.CrossEntropyLoss()
                    optimiser_0 = torch.optim.SGD(sequential_model.parameters(), lr=0.01)
                    MNISTDigitsDataset = datasets.MNIST(root="data", train=True, download=True, transform=ToTensor())
                    MNISTDigits = DataLoader(MNISTDigitsDataset, batch_size=16)
                    for data, labels in MNISTDigits:
                        train_prediction = sequential_model(data)
                        loss = CrossEntropy_0(train_prediction, labels)
                        loss.backward()
                        optimiser_0.step()
                        optimiser_0.zero_grad()
                class sequential_model_0(nn.Module):
                	def __init__(self):
                		super().__init__()
                		self.flatten = nn.Flatten()
                		self.stack = sequential_model
                	def forward(self, x):
                		if len(x.size()) > 1:
                			x = self.flatten(x)
                		logits = self.stack(x)
                		return logits
                train_0(sequential_model_0().to("cpu"))""";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testTrainSGDManual() {
        String code = "sequential_model = sequential(linear(5, 10)); train { train_prediction = sequential_model([1.0, 2.0, 3.0, 4.0, 5.0]); loss = CE(train_prediction, [1.0, 0.0, 0.0, 0.0, 0.0]); SGD(sequential_model, loss, 0.01); }";
        String expected = """     
                sequential_model = nn.Sequential(nn.Linear(5, 10))
                def train_0(sequential_model):
                    train_prediction = sequential_model(torch.tensor([1.0, 2.0, 3.0, 4.0, 5.0]))
                    CrossEntropy_0 = nn.CrossEntropyLoss()
                    loss = CrossEntropy_0(train_prediction, torch.tensor([1.0, 0.0, 0.0, 0.0, 0.0]))
                    optimiser_0 = torch.optim.SGD(sequential_model.parameters(), lr=0.01)
                    loss.backward()
                    optimiser_0.step()
                    optimiser_0.zero_grad()
                class sequential_model_0(nn.Module):
                	def __init__(self):
                		super().__init__()
                		self.flatten = nn.Flatten()
                		self.stack = sequential_model
                	def forward(self, x):
                		if len(x.size()) > 1:
                			x = self.flatten(x)
                		logits = self.stack(x)
                		return logits
                train_0(sequential_model_0().to("cpu"))""";
        assert (testCompileDefine(code, expected));
    }

    /*==================================================================*
     *                           Expressions                            *
     *==================================================================*/

    /*============================= Define =============================*/

    /* ============== Id ==============*/
    @Test
    public void testDefineExpressionId() {
        String code = "a = 1; b = a;";
        String expected = "a = 1\nb = a";
        assert (testCompileDefine(code, expected));
    }

    /* ============== Activation ==============*/

    @Test
    public void testDefineExpressionActivation() {
        String code = "a = ReLU; b = Sigmoid;";
        String expected = "a = nn.ReLU()\nb = nn.Sigmoid()";
        assert (testCompileDefine(code, expected));
    }

    /* ============== Layer ==============*/

    @Test
    public void testDefineExpressionLayer() {
        String code = "a = linear(10, 10);";
        String expected = "a = nn.Linear(10, 10)";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testDefineExpressionLayerId() {
        String code = "size = 10; a = linear(size, size);";
        String expected = "size = 10\na = nn.Linear(size, size)";
        assert (testCompileDefine(code, expected));
    }

    /* ============== Sequential ==============*/

    @Test
    public void testDefineExpressionModel() {
        String code = "a = sequential(linear(10, 10));";
        String expected = "a = nn.Sequential(nn.Linear(10, 10))";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testDefineExpressionModelChain() {
        String code = "a = sequential(linear(10, 10) -> ReLU -> linear(10, 20) -> ReLU -> Sigmoid -> linear(20, 40));";
        String expected = "a = nn.Sequential(nn.Linear(10, 10), nn.ReLU(), nn.Linear(10, 20), nn.ReLU(), nn.Sigmoid(), nn.Linear(20, 40))";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testDefineExpressionModelChainId() {
        String code = "l = ReLU; a = sequential(linear(10, 10) -> l -> linear(10, 20) -> l -> Sigmoid -> linear(20, 40));";
        String expected = "l = nn.ReLU()\na = nn.Sequential(nn.Linear(10, 10), l, nn.Linear(10, 20), l, nn.Sigmoid(), nn.Linear(20, 40))";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testDefineExpressionModelChainId2() {
        String code = "l = linear(10, 10); a = sequential(l -> ReLU -> l); b = sequential(l);";
        String expected = "l = nn.Linear(10, 10)\na = nn.Sequential(l, nn.ReLU(), l)\nb = nn.Sequential(l)";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testDefineExpressionModelChainLayerCombine() {
        String code = "a = sequential(linear(10, 10) -> linear(10, 20) -> linear(20, 30) -> ReLU -> Sigmoid -> linear(30, 40));";
        String expected = "a = nn.Sequential(nn.Linear(10, 30), nn.ReLU(), nn.Sigmoid(), nn.Linear(30, 40))";
        assert (testCompileDefine(code, expected));
    }

    /* ============== Number ==============*/
    @Test
    public void testDefineExpressionNumber() {
        String code = "a = 1;";
        String expected = "a = 1";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testDefineExpressionNumberPlus() {
        String code = "a = 1 + 2;";
        String expected = "a = 3";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testDefineExpressionNumberMinus() {
        String code = "a = 1 - 2;";
        String expected = "a = -1";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testDefineExpressionNumberMinusPlus() {
        String code = "a = 1 - 2 + 2;";
        String expected = "a = 1";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testDefineExpressionNumberDivision() {
        String code = "a = 1 / 2;";
        String expected = "a = 0.5";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testDefineExpressionNumberDivisionPlus() {
        String code = "a = 1 + 1 / 2;";
        String expected = "a = 1.5";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testDefineExpressionNumberDivisionPlus2() {
        String code = "a = 1 / 2 + 1;";
        String expected = "a = 1.5";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testDefineExpressionNumberDivisionMinus() {
        String code = "a = 1 - 1 / 2;";
        String expected = "a = 0.5";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testDefineExpressionNumberDivisionMinus2() {
        String code = "a = 1 / 2 - 1;";
        String expected = "a = -0.5";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testDefineExpressionNumberMultiplication() {
        String code = "a = 2 * 2;";
        String expected = "a = 4";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testDefineExpressionNumberMultiplicationPlus() {
        String code = "a = 1 + 5 * 2;";
        String expected = "a = 11";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testDefineExpressionNumberMultiplicationPlus2() {
        String code = "a = 5 * 2 + 1;";
        String expected = "a = 11";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testDefineExpressionNumberMultiplicationMinus() {
        String code = "a = 1 - 5 * 2;";
        String expected = "a = -9";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testDefineExpressionNumberMultiplicationMinus2() {
        String code = "a = 5 * 2 - 1;";
        String expected = "a = 9";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testDefineExpressionNumberMultiplicationDivision() {
        String code = "a = 5 * 1 / 2;";
        String expected = "a = 2.5";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testDefineExpressionNumberMultiplicationDivision2() {
        String code = "a = 1 / 2 * 5;";
        String expected = "a = 2.5";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testDefineExpressionNumberId() {
        String code = "b = 5; a = 1 / 2 * b;";
        String expected = "b = 5\na = (0.5 * b)";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testDefineExpressionNumberId2() {
        String code = "b = 5; a = b / 2 * b;";
        String expected = "b = 5\na = ((b / 2) * b)";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testDefineExpressionNumberId3() {
        String code = "b = 5; a = b / (2 * b) + b;";
        String expected = "b = 5\na = ((b / (2 * b)) + b)";
        assert (testCompileDefine(code, expected));
    }

    /* ============== String ==============*/
    @Test
    public void testDefineExpressionString() {
        String code = "a = \"test\";";
        String expected = "a = \"test\"";
        assert (testCompileDefine(code, expected));
    }

    /* ============== Number array ==============*/

    @Test
    public void testDefineExpressionNumberArray() {
        String code = "a = [1.0, 2.0, 3.0];";
        String expected = "a = torch.tensor([1.0, 2.0, 3.0])";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testDefineExpressionNumberArrayId() {
        String code = "b = 1.0; a = [b, 2.0, 3.0];";
        String expected = "b = 1.0\na = torch.tensor([b, 2.0, 3.0])";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testDefineExpressionNumberArrayArithmetic() {
        String code = "a = [2 + 10 / 2, 2.0, 3.0];";
        String expected = "a = torch.tensor([7.0, 2.0, 3.0])";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testDefineExpressionNumberArrayArithmeticId() {
        String code = "b = 2.0; a = [b + 10 / 2, 2.0, b];";
        String expected = "b = 2.0\na = torch.tensor([(b + 5.0), 2.0, b])";
        assert (testCompileDefine(code, expected));
    }

    /* ============== Boolean ==============*/

    @Test
    public void testDefineExpressionBoolean() {
        String code = "a = true;";
        String expected = "a = True";
        assert (testCompileDefine(code, expected));
    }

    private String getRandomEquality() {
        String[] operators = {"<", ">", "<=", ">=", "==", "!="};
        int index = new Random().nextInt(operators.length);
        return operators[index];
    }

    private String getRandomOperator() {
        String[] operators = {"and", "or"};
        int index = new Random().nextInt(operators.length);
        return operators[index];
    }

    @Test
    public void testDefineExpressionBooleanExpression() {
        String equality = getRandomEquality();
        System.out.printf("[testDefineExpressionBooleanExpression] Equality used: %s\n", equality);
        String code = "a = 2 " + equality + " 2;";
        String expected = "a = (2 " + equality + " 2)";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testDefineExpressionBooleanExpressionMulti() {
        String operator = getRandomOperator();
        String equality = getRandomEquality();
        System.out.printf("[testDefineExpressionBooleanExpressionMulti] Equality used: %s, Operator used: %s\n", equality, operator);
        String code = "a = 2 " + equality + " 2 " + operator + " true;";
        String expected = "a = ((2 " + equality + " 2) " + operator + " True)";
        assert (testCompileDefine(code, expected));
    }

    /*==================================================================*
     *                        Model expressions                         *
     *==================================================================*/

    /*
    | 'MSE' '(' expressionTrain ',' expressionTrain ')'                 #ExpressionMSE
    | 'CrossEntropy' '(' expressionTrain ',' expressionTrain ')'        #ExpressionCrossEntropy
    * */

    @Test
    public void testCrossEntropy() {
        String code = "test = sequential(linear(10, 10)); train { pred = test([1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0]); loss = CE(pred, [1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0]); } ";
        String expected = """
                test = nn.Sequential(nn.Linear(10, 10))
                def train_0(test):
                    pred = test(torch.tensor([1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0]))
                    CrossEntropy_0 = nn.CrossEntropyLoss()
                    loss = CrossEntropy_0(pred, torch.tensor([1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0]))
                class test_0(nn.Module):
                	def __init__(self):
                		super().__init__()
                		self.flatten = nn.Flatten()
                		self.stack = test
                	def forward(self, x):
                		if len(x.size()) > 1:
                			x = self.flatten(x)
                		logits = self.stack(x)
                		return logits
                train_0(test_0().to("cpu"))""";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testMSE() {
        String code = "test = sequential(linear(10, 10)); train { pred = test([1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0]); loss = MSE(pred, [1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0]); } ";
        String expected = """
                test = nn.Sequential(nn.Linear(10, 10))
                def train_0(test):
                    pred = test(torch.tensor([1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0]))
                    MSE_0 = nn.MSELoss()
                    loss = MSE_0(pred, torch.tensor([1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0]))
                class test_0(nn.Module):
                	def __init__(self):
                		super().__init__()
                		self.flatten = nn.Flatten()
                		self.stack = test
                	def forward(self, x):
                		if len(x.size()) > 1:
                			x = self.flatten(x)
                		logits = self.stack(x)
                		return logits
                train_0(test_0().to("cpu"))""";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testCalculateAccuracy() {
        String code = "test = sequential(linear(10, 10)); train { pred = test([1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0]); acc = calculate_accuracy(pred, [1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0]); } ";
        String expected = """
                test = nn.Sequential(nn.Linear(10, 10))
                def train_0(test):
                    pred = test(torch.tensor([1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0]))
                    acc = (pred.argmax(1) == torch.tensor([1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0])).type(torch.float).sum().item()
                class test_0(nn.Module):
                	def __init__(self):
                		super().__init__()
                		self.flatten = nn.Flatten()
                		self.stack = test
                	def forward(self, x):
                		if len(x.size()) > 1:
                			x = self.flatten(x)
                		logits = self.stack(x)
                		return logits
                train_0(test_0().to("cpu"))""";
        assert (testCompileDefine(code, expected));
    }

    @Test
    public void testModelCall() {
        String code = "test = sequential(linear(10, 10)); train { pred = test([1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0]); }";
        String expected = """
                test = nn.Sequential(nn.Linear(10, 10))
                def train_0(test):
                    pred = test(torch.tensor([1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0]))
                class test_0(nn.Module):
                	def __init__(self):
                		super().__init__()
                		self.flatten = nn.Flatten()
                		self.stack = test
                	def forward(self, x):
                		if len(x.size()) > 1:
                			x = self.flatten(x)
                		logits = self.stack(x)
                		return logits
                train_0(test_0().to("cpu"))""";
        assert (testCompileDefine(code, expected));
    }
}
