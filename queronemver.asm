global main
extern printf
extern scanf

section .text
main: 	; Entrada do programa
	push ebp
	mov ebp, esp
	sub esp, 20
	push rotuloString1
	call printf
	add esp, 4
	push rotuloStringLN
	call printf
	add esp, 4
	push 1
	pop eax
	mov dword[ebp - 0], eax
	push 1
	pop eax
	mov dword[ebp - 4], eax
	push 0
	pop eax
	mov dword[ebp - 16], eax
	push 0
	pop eax
	mov dword[ebp - 8], eax
	push rotuloString2
	call printf
	add esp, 4
	mov edx, ebp
	lea eax, [edx - 16]
	push eax
	push @Integer
	call scanf
	add esp, 8
	push dword[ebp - 0]
	push @Integer
	call printf
	add esp, 8
	push rotuloStringLN
	call printf
	add esp, 4
	push dword[ebp - 4]
	push @Integer
	call printf
	add esp, 8
	push rotuloStringLN
	call printf
	add esp, 4
	push 2
	pop eax
	mov dword[ebp - 12], eax
rotuloWhile3: 	push dword[ebp - 12]
	push dword[ebp - 16]
	pop eax
	cmp dword [ESP], eax
	jg rotuloFalsoREL5
	mov dword [ESP], 1
	jmp rotuloSaidaREL6
rotuloFalsoREL5: 	mov dword [ESP], 0
rotuloSaidaREL6: 	cmp dword[esp], 0
	je rotuloFimWhile4
	push dword[ebp - 0]
	push dword[ebp - 4]
	pop eax
	add dword[ESP], eax
	pop eax
	mov dword[ebp - 8], eax
	push dword[ebp - 8]
	push @Integer
	call printf
	add esp, 8
	push rotuloStringLN
	call printf
	add esp, 4
	push dword[ebp - 4]
	pop eax
	mov dword[ebp - 0], eax
	push dword[ebp - 8]
	pop eax
	mov dword[ebp - 4], eax
	push dword[ebp - 12]
	push 1
	pop eax
	add dword[ESP], eax
	pop eax
	mov dword[ebp - 12], eax
	jmp rotuloWhile3
rotuloFimWhile4: 	leave
	ret

section .data

rotuloString1: db '----------FIBONACCI------------',0
rotuloStringLN: db '',10,0
rotuloString2: db 'Informe a quantidade de numeros que deseja ver da sequencia fibonacci: ',0
@Integer: db '%d',0
