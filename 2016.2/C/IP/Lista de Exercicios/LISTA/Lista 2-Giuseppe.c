/*

1.

main()
{
    int A, B;

    printf("Digite dois valores A e B: ");
    scanf("%i %i", &A, &B);

    if( A%B== 0 )
    {
        printf("Numeros A e divisivel por B");
    }
    else
    {
        printf("Os numeros nao sao divisiveis");
    }

}

2
main()
{
    int var1;

    printf("Digite um valor: ");
    scanf("%i", &var1);

    if( var1>=20 )
    {
        printf("%i", var1);
    }

}

3
main()
{
    int A, B, soma;

    printf("Digite dois valores A e B: ");
    scanf("%i %i", &A, &B);

    soma= A+B;

    if (soma>= 10)
    {
        printf("%i", soma);
    }

}

4
main()
{
    int A, B, soma, fim;

    printf("Digite dois valores A e B: ");
    scanf("%i %i", &A, &B);

    soma= A+B;

    if (soma>= 20)
    {
        fim= soma + 8;
        printf("%i", fim);
    }
    else
    {
        fim= soma - 5;
        printf("%i", fim);
    }
}

5
main()
{
	int salar, prest, imprest;

	printf("Digite o seu salario: ");
	scanf("%i", &salar);

	printf("Digite o valor da sua prestacao: ");
	scanf("%i", &prest);

    imprest= salar*0,3;

    if(imprest<= prest)
    {
        printf("imprestimo concedido!!");
    }
    else
    {
        printf("Imprestimo nao concedido ");
    }

}

6 
main()
{
	int numero;
	printf("Digite um numero: ");
	scanf("%i", &numero);
	if(numero>=20 && numero<=90)
	{ 
		printf("Ta entre 20 e 90");
	}
	else
	{
		printf("NAO ta entre 20 e 90");
	}
}
7
main()
{
	int idade;
	char sexo;
	printf("Digite sua idade: ");
	scanf("%i", &idade);
	getchar();
	printf("Digite seu sexo: [M/F]");
	scanf("%c", &sexo);
	
	if(idade<=25 && sexo=='f' || sexo=='F')
	{ 
		printf("ACEITAA");
	}
	else
	{
		printf("NAO ACEITAA");
	}
}
8
main()
{
	int var1, var2;	
	
	printf("Digite dois valores: ");
	scanf("%i %i", &var1, &var2);
	
	if(var1>var2)
	{
		printf("%i , %i", var1, var2);
	}
	else
	{
		printf("%i , %i", var2, var1);
	}
	
}
9
#include<stdio.h>
#include<stdlib.h>
main()
{
	int a,b,c,d;
	printf("Digite 3 numeros separados por '-': ");
	scanf("%i-%i-%i", &a,&b,&c);
	
	if(a>b && a>c)
	{
		d=a;
		printf("O maior eh: %i", d);
	}
	else if(b>a && b>c)
	{
		d=b;
		printf("O maior eh: %i", d);
	}
	else if(c>b && c>a)
	{
		d=c;
		printf("O maior eh: %i", d);
	}
}

10.

main()
{
    int dia, valor;
    float taxa, valf;
    printf("Digite o número de dias: ");
    scanf("%i", &dia);

    valor= 50.00* dia;

    if(dia>15)
    {
    taxa= 15*dia;
    valf= taxa + valor;
    printf(" o valor a pagar eh: %.2f", valf);
    }

    if(dia=15)
    {
    taxa= 10*dia;
    valf= taxa + valor;
    printf(" o valor a pagar eh: %.2f", valf);
    }

    if(dia<15)
    {
    taxa= 15.30*dia;
    valf= taxa + valor;
    printf(" o valor a pagar eh: %.2f", valf);
    }
}

11.

main()
{
	int idade;
	printf("Digite sua idade: ");
	scanf("%i", &idade);
	if(idade<=10)
	{
		printf("R$30.00");
	}
	else 	if(idade>10 && idade<=29)
	{
		printf("R$60.00");
	}
	else 	if(idade>29 && idade<=45)
	{
		printf("R$120.00");
	}
	else 	if(idade>45 && idade<=59)
	{
		printf("R$150.00");
	}
	else 	if(idade>59 && idade<=65)
	{
		printf("R$250.00");
	}
	else 	if(idade>65)
	{
		printf("R$400.00");
	}
	else
	{
		printf("idade invalida");
	}
}




12.
#include<stdio.h>
#include<stdlib.h>

main()
{
	int mes;
	
	printf("Digite um numero de 1 a 12: ");
	scanf("%i", &mes);
	
	switch (mes)
	{
		case 1:
			printf("janeiro\n");
			break;
		case 2:
			printf("fevereiro\n");
			break;
		case 3:
			printf("marco\n");
			break;
		case 4:
			printf("abril\n");
			break;
		case 5:
			printf("maio\n");
			break;
		case 6:
			printf("junho\n");
			break;
		case 7:
			printf("julho\n");
			break;
		case 8:
			printf("agosto\n");
			break;
		case 9:
			printf("setembro\n");
			break;
		case 10:
			printf("outubro\n");
			break;
		case 11:
			printf("novembro\n");
			break;
		case 12:
			printf("dezembro\n");
			break;
		default:
		printf("valor invalido");
		
	}
}



