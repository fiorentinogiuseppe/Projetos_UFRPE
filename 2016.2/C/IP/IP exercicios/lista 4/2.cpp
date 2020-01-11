#include<stdio.h>
#include<stdlib.h>

main()
{
	int tamanho,i, big=0, less=1000;
	printf("Digite o tamanho do vetor: ");
	scanf("%i", &tamanho);
	
	int m[tamanho];
	for(i=0; i<tamanho; i++)
	{
		printf("Digite o valor de m[%i]: ", i);
		scanf("%i", &m[i]);
	}
	for(i=0; i<tamanho;i++)
	{
		if(m[i]>big)
		{
			big=m[i];
		}
		else if(m[i]<less)
		{
			less=m[i];
		}
	}
	printf("O maior valor foi: %i\nO menor valor foi: %i", big, less);
}
