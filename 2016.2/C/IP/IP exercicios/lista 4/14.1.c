#include<stdio.h>
#include<stdlib.h>

main()
{
	float nota1[20];
	float nota2[20];
	float media[20];
	int i,j;


	for(i=0;i<20;i++)
	{
		    printf("Digite a nota 1 do aluno %i: ", i+1);
		    scanf("%f", &nota1[i]);
	}
    system("cls");
    for(i=0;i<20;i++)
	{
		    printf("Digite a nota 2 do aluno %i: ", i+1);
		    scanf("%f", &nota2[i]);
	}
    system("cls");
	 for(i=0;i<20;i++)
	{
        media[i]= ((nota1[i]*2)+(nota2[i]*3))/5;
        printf("A media do aluno %i: %.2f\n",i, media[i]);
	}
}
