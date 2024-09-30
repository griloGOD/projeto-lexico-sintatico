{ Programa que calcula o salario de funcionarios }
Program CalcularSalario;

Var
    Salario, HorasTrabalhadas, ValorPorHora: integer;  { Declaração de variáveis }

Begin
    { Corpo do programa principal }
    writeln('Digite o numero de horas trabalhadas:');
    read(HorasTrabalhadas);
    writeln('Digite o valor por hora:');
    read(ValorPorHora);
    Salario := HorasTrabalhadas * ValorPorHora;  { Cálculo do salário }
    writeln('O salario calculado é: ', Salario);
End.



{' sdasdAa A '
Program CalcularSalario;
Var TempoEmAnos, ValorSalario, a, b, c : Integer; 
Begin 
    If (TempoEmAnos > 10) Then
    Begin
        ValorSalario := 100; 
    End
    Else Begin
        ValorSalario := ValorSalario * 2;
    End;
    Write(ValorSalario);
    c := 1;
    while (c <= 10) do
    begin
        write(c);
        c := c + 1;
    end;
    c := 1;
    repeat
        write(c);
        c := c + 1;
    until (c > 10);
    for c := 1 to 10 do
    begin
        write(c);
    end;
    if (a > 0 and b > 0) then
    begin
        writeln('Positivos');
    end
    else
    begin
        writeln('Um dos valores não é positivo');
    end;
End.}