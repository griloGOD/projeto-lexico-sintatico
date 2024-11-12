Program CalcularSalario;
{Var TempoEmAnos, ValorSalario, a, b, c : Integer;} 
Var termo1, termo2, aux, cont, quantos : Integer;
Begin
    writeln('----------FIBONACCI------------');
    termo1 := 1;
    termo2 := 1;
    quantos := 0;
    aux := 0;
    write('Informe a quantidade de numeros que deseja ver da sequencia fibonacci: ');
    read(quantos);
    writeln(termo1);
    writeln(termo2);
    cont := 2;
    while (cont <= quantos) do
    begin
        aux := termo1 + termo2;
        writeln(aux);    
        termo1 := termo2;
        termo2 := aux;
        cont := cont + 1; 
    end; 

    {If (TempoEmAnos > 10) Then
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
    end;}
End.