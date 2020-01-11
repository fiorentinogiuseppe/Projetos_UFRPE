#include<stdio.h>
#include<stdlib.h>
#include<string.h>

main()
{
	int tamanho,i,j,k,a;
	printf("Digite o tamanho dos vetores: ");
	scanf("%i", &tamanho);
	int v[tamanho];
	char s[tamanho];

	system("cls");
	printf("Digite os valores do vetor de inteiros!!\n");
	for(i=0;i<tamanho;i++)
	{
		printf("Digite v[%i]: ", i);
		scanf("%i", &v[i]);
	}
	system("cls");
	printf("Digite os valores da string\n");
	getchar();
	for(i=0;i<tamanho;i++)
	{
		printf("Digite o s[%i]: ",i);
		scanf("%c", &s[i]);
		getchar();
	}
	
		for(j=0; j<tamanho;j++)
	{
		a=v[j];
		for(k=0; k<a; k++)
		{
			printf("%c", s[k]);
		}
		printf("\n");
	}

}
