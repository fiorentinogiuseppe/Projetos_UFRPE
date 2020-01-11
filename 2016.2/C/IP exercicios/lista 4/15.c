#include<stdio.h>
#include<stdlib.h>

main()
{
	float nota1[20];
	float nota2[20];
	float media[20];
	int i,j, contador=0;
	float mediaturma=0, mediaturmafinal;


	for(i=0;i<5;i++)
	{
		    printf("Digite a nota 1 do aluno %i: ", i+1);
		    scanf("%f", &nota1[i]);
	}
    system("cls");
    for(i=0;i<5;i++)
	{
		    printf("Digite a nota 2 do aluno %i: ", i+1);
		    scanf("%f", &nota2[i]);
	}
    system("cls");
	 for(i=0;i<5;i++)
	{
        media[i]= ((nota1[i]*2)+(nota2[i]*3))/5;
        printf("A media do aluno %i: %.2f\n",i+1, media[i]);
	}
	for(i=0;i<5;i++)
	{
		mediaturma+= media[i];
	}
	mediaturmafinal=mediaturma/5;
	printf("os alunos que ficaram abaixo da media foram: \n");
	for(i=0;i<5;i++)
	{
		if(media[i] < mediaturmafinal)
		{
			contador++;
			printf("o aluno %i;\n",i);
		}
	}
	printf("\nUm total de %i alunos", contador);
}

