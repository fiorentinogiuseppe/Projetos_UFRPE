/*

1)
NOTA=6
MEDIA=3
TOTAL=22
K=14
Z=-1/5
I=73
A=-4;

2)
a. F
b. V
c. F
d. V 
e. F
f. F
g. F


main()
{
    printf("=========Questao 3=========\n\n");
	printf("eu preciso fazer todos os algoritmos para aprender");
}


main()
{
    printf("=========Questao 4=========\n\n");
	int var1, var2, var3;
	var1= 28;
	var2= 43;
	var3= (28*43);

	printf("o valor do produto e: %i", var3);

}

main()
{   printf("=========Questao 5=========\n\n");
	printf("Digite dois valores: ");
	int var1, var2;
	scanf("%i-%i", &var1, &var2);

	printf("os numeros foram: Var1=%i, Var2= %i", var1, var2);

}


main()
{
    printf("=========Questao 6=========\n"\n);
	printf("Digite um valores: ");
	int var1;
	scanf("%i", &var1);

	int ante, suce;
	ante= var1-1;
	suce= var1+1;

	printf("o seu antecessor e %i, sendo %i seu sucessor", ante, suce);

}
*/

main()
{
    printf("=========Questao 7=========\n\n");
    char nome[20];

	printf("Digite seu nome: ");
	scanf("%s", nome);

    int telefo[200];

	printf("Digite seu telefone: ");
	scanf("%i", telefo);

    char end[20];

	printf("Voce mora na Rua: ");
	scanf("%s", end);


	printf("o seu Nome eh %s. Voceh mora em %s e seu telefone eh %i", nome, end, telefo);

	}

/*
main()
{
    printf("=========Questao 8=========\n\n");
	printf("Digite dois valores: ");
	int var1, var2, var3;
	scanf("%i-%i", &var1, &var2);

	var3= (var1+var2);

	printf("os numeros foram: Var1=%i, Var2= %i. E a soma eh: %i", var1, var2, var3);

}


main()
{
    printf("=========Questao 9=========\n\n");
	printf("Digite dois valores: ");
	int var1, var2, var3,var4, var5;
	scanf("%i-%i-%i-%i", &var1, &var2, &var3, &var4);

	var5= (var1+var2+var3+var4);

	printf("A soma eh: %i", var5);

}


main()
{
    printf("=========Questao 10=========\n\n");
	printf("Digite as 3 notas: ");
	int var1, var2, var3,var4;
	scanf("%i-%i-%i-%i", &var1, &var2, &var3);

	var4= ((var1+var2+var3)/3);

	printf("A media eh: %i", var4);

}


main()
{
    printf("=========Questao 11=========\n\n");
	printf("Digite as 3 notas: \n");
	int var1, var2, var3,var4;
	scanf("%i-%i-%i-%i", &var1, &var2, &var3);

	printf("Digite seus respectivos pesos: \n");
	int pe1,pe2,pe3;
	scanf("%i-%i-%i", &pe1,&pe2, &pe3);

	var4= (((var1*pe1)+(var2*pe2)+(var3*pe3))/(pe1+pe2+pe3));

	printf("A media eh: %i", var4);

}


main()
{
    printf("=========Questao 12=========\n\n");
	printf("Digite seu salario: ");
	int var1, Nsal;
	scanf("%i", &var1);

	Nsal= (var1*1.25);

	printf("O seu novo salario e %i", Nsal);

}

printf("=========Questao 13=========\n");


main();
{
    int var1;

	printf("Digite seu salario: ");
	scanf("%i", &var1);

    float aum, Nsal;
	printf("Digite um percentual de aumento: ");
	scanf("%f", &aum);

	Nsal= (var1*aum);

	printf("O seu novo salario e %f", Nsal);

}

printf("=========Questao 14=========\n");


main();
{
    int var1;

	printf("Digite seu salario: ");
	scanf("%i", &var1);

    float Nsal, grat, imp;

	grat = var1*0.05;

	imp= var1*0.07;

	Nsal= (var1+grat-imp);

	printf("O seu novo salario e %f", Nsal);

}



main()
{
    printf("=========Questao 15=========\n\n");
    int var1;

	printf("Digite seu salario: ");
	scanf("%i", &var1);

    float Nsal, imp;

	imp= var1*0.01;

	Nsal= (var1+50-imp);

	printf("O seu novo salario e %f", Nsal);

}


main()
{
    printf("=========Questao 16=========\n\n");
    float depo, jur, tot, rend;

    printf("Digite o valor do deposito: ");
    scanf("%f", &depo);
    printf("Digite o valor do juros: ");
    scanf("%f", &jur);

    rend= depo*(jur/100);
    tot= rend + depo;

    printf("o valor total depois do rendimento e %.1f: ", tot);

}


#include<stdio.h>
#include<stdlib.h>
#include<math.h>
main()
{
    printf("=========Questao 17=========\n\n");
	
	
		int l1,l2,l3;
		float h, area,x ;
		
		printf("Digite o valor do lado 1: ");
		scanf("%i", &l1);
		printf("Digite o valor do lado 2: ");
		scanf("%i", &l2);
		printf("Digite o valor do lado 3: ");
		scanf("%i", &l3);
		if(l1<l2+l3 && l2<l1+l2 && l3<l1+l2)
		{
			x= pow(l2,2) - pow(l3/2,2);
			printf("%f", x);
			h= sqrt(x);
			printf("%f", h);
			
			area=(l3*h)/2;
			printf(" A area do triangulo eh: %.3f\n", area);
		}
		else{
			printf("Essas medidas nao formam um triangulo");
		}
}

main()
{
    printf("=========Questao 18=========\n\n");
	printf("Digite o raio da circunferencia: \n");
	int r;
	scanf("%i", &r);

	float pi, area;

	pi=3.1415;

	area=(pi*(r*r));

	printf("A area eh: %f", area);


}


main()
{
    printf("=========Questao 19=========\n\n");
    int anoN, anoA, ida,idaF;

    printf("Digite o ano de seu nascimento: ");
    scanf("%i", &anoN);


    printf("Digite o ano atual: ");
    scanf("%i", &anoA);

    ida= anoA- anoN;

    idaF= 2020-anoN;

    printf("-Sua idade atual e: %i;\n-Voce nasceu em: %i;\n-Sua idade em 2020 e: %i.", ida, anoN, idaF);


}



main()
{
    printf("=========Questao 20=========\n\n");
    int CarroN, Pfab, Luc, imp, impt, LucR;

    printf("Escreva o preço de fabrica do veiculo: ");
    scanf("%i", &Pfab);

    printf("Escreva o porcentual de lucro do veiculo: ");
    scanf("%i", &Luc);

    printf("Escreva o porcentual de imposto do veiculo: ");
    scanf("%i", &imp);

    CarroN= Pfab + Luc + impt;
    LucR= Pfab+ Luc;
    impt= Pfab*imp/100;

    printf("O valor correspondente ao lucro do distribuidor e: %i\n;O imposto e: %i;\nO valor final do carro e: %i.", LucR, impt, CarroN);


}



main()
{
    printf("=========Questao 21=========\n\n");
    int hora, recebe, salaM, imp, vhora, salabru;

    printf("Escreva o numero de horas de trabalho: ");
    scanf("%i", &hora);

    printf("Escreva o valor do salario minimo:");
    scanf("%i", &salaM);

    vhora= salaM/2;

    salabru= vhora*hora;

    imp= salabru*0.03;

    recebe= salabru- imp;

    printf("O salario a receber e: %i", recebe);
}


main()
{
    printf("=========Questao 22=========\n");
    int tam, subi, degra;

    printf("Escreva o tamanho do degrau em cm: ");
    scanf("%i", &tam);

    printf("Que altuma deseja subir? (em cm): ");
    scanf("%i", &subi);

    degra= subi/tam;

    printf("você precisa subir: %i", degra);

}


*/

