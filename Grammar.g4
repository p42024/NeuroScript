grammar Grammar;


program
    : state* EOF
    ;


state
    : train     #TrainSection
    | define    #DefineSection
    ;


train
    : 'train' scopeTrain
    ;


define
    : statementDefine
    ;


statementDefine
    : id '=' expressionDefine ';'                           #StatementDefineAssignment
    | 'loop' scopeDefine                                    #StatementDefineLoop
    | 'break' ';'                                           #StatementDefineBreak
    | 'if' bool scopeDefine elseDefine?                     #StatementDefineIf
    | 'export' 'model' expressionDefine 'as' stringOrId ';' #StatementDefineExport
    | 'import' 'model' stringOrId 'as' id  ';'              #StatementDefineImportModel
    | 'print' '(' expressionDefine ')' ';'                  #StatementDefinePrint
    ;


statementTrain
    : id '=' expressionTrain ';'                                                #StatementTrainAssignment
    | 'loop' scopeTrain                                                         #StatementTrainLoop
    | 'break' ';'                                                               #StatementTrainBreak
    | 'if' bool scopeTrain elseTrain?                                           #StatementTrainIf
    | 'print' '(' expressionTrain ')' ';'                                       #StatementTrainPrint
    | 'SGD' '(' expressionTrain ',' expressionTrain ',' expressionTrain ')' ';' #StatementTrainSGD
    | 'export' 'model' expressionTrain 'as' stringOrId ';'                      #StatementTrainExport
    | 'import' 'data' stringOrId 'as' id  ';'                                   #StatementTrainImportData
    | 'import' 'model' stringOrId 'as' id  ';'                                  #StatementTrainImportModel
    ;


stringOrId: id | string ;


elseTrain
    : 'else' scopeTrain
    ;


elseDefine
    : 'else' scopeDefine
    ;


scopeTrain
    : '{' statementTrain* '}'
    ;

scopeDefine
    : '{' statementDefine* '}'
    ;


expressionDefine
    : id                        #ExpressionId
    | layer                     #ExpressionLayer
    | model                     #ExpressionModel
    | number                    #ExpressionNumber
    | string                    #ExpressionString
    | numberArray               #ExpressionNumberArray
    | bool                      #ExpressionBoolean
    | activation                #ExpressionActivation
    | '(' expressionDefine ')'  #ExpressionDefineParenthesis
    ;


expressionTrain
    : id                       #ExpressionTrainId
    | number                   #ExpressionTrainNumber
    | string                   #ExpressionTrainString
    | numberArray              #ExpressionTrainNumberArray
    | bool                     #ExpressionTrainBoolean
    | '(' expressionTrain ')'  #ExpressionTrainParenthesis

    // Functions
    | 'calculate_accuracy' '(' expressionTrain ',' expressionTrain ')'  #ExpressionAccuracy
    | 'MSE' '(' expressionTrain ',' expressionTrain ')'                 #ExpressionMSE
    | 'CE' '(' expressionTrain ',' expressionTrain ')'                  #ExpressionCrossEntropy
    | id '(' expressionTrain ')'                                        #ExpressionModelCall
    ;


bool
    : andor
    ;


andor
    : ordering (andorOp ordering)*
    ;


andorOp
    : 'and'     #AndorOpAnd
    | 'or'      #AndorOpOr
    ;


ordering
    : boolConst orderingOp boolConst    #OrderingOrdering
    | boolConst                         #OrderingConst
    ;


orderingOp
    : '>'       #OrderingOpGe
    | '>='      #OrderingOpGeq
    | '<'       #OrderingOpLe
    | '<='      #OrderingOpLeq
    | '=='      #OrderingOpEq
    | '!='      #OrderingOpNeq
    ;


boolConst
    : 'true'        #BoolConstTrue
    | 'false'       #BoolConstFalse
    | id            #BoolConstId
    | number        #BoolConstNumber
    | '(' bool ')'  #BoolConstBool
    ;


model
    : sequentialContainer
    ;


sequentialContainer
    : 'sequential' '(' sequentialConst ('->' (activation | sequentialConst))* ')' #SequentialContainerModel
    ;


sequentialConst
    : layer         #SequentialConstLayer
    | model         #SequentialConstModel
    | id            #SequentialConstId
    ;


activation
    : 'ReLU'        #ActivationReLU
    | 'Tanh'        #ActivationTanh
    | 'Sigmoid'     #ActivationSigmoid
    ;


layer
    : linearLayer
    ;


linearLayer
    : 'linear' '(' linearLayerConst ',' linearLayerConst ')'
    ;


linearLayerConst
    : number        #LinearLayerConstNumber
    | id            #LinearLayerConstId
    ;


id
    :   ID
    ;


string
    : STRING
    ;


number
    : additive
    ;


additive
    : multiplicative (addOp multiplicative)*
    ;


multiplicative
    : numberConstant (multOp numberConstant)*
    ;


multOp
    : '/'   #MultOpDivision
    | '*'   #MultOpMultiplication
    ;


addOp
    : '+'   #AddOpAdd
    | '-'   #AddOpSub
    ;


numberConstant
    : id                #NumberConstantId
    | int               #NumberConstantInt
    | float             #NumberConstantFloat
    | '(' number ')'    #NumberConstantParenthesis
    ;

numberArray: '[' number (',' number)* ']' ;

int
    : INT
    ;


float
    : FLOAT
    ;


INT
    : ('-')?[0-9]+
    ;


FLOAT
    : '-'?[0-9]+'.'[0-9]+
    ;


ID
    : [a-zA-Z_][a-zA-Z_0-9]*
    ;


STRING
    : '"' (~["] | '\\"' )* '"'
    ;


WS
    : [ \t\r\n]+ -> skip
    ;


COMMENT
    : '//' (~'\n')* -> skip
    ;


MULTILINECOMMENT
    : '/*' .*? '*/' -> skip
    ;
