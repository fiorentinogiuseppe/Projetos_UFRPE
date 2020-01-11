#include<stdio.h>
#include<stdlib.h>

main()
{
	int colegio[20][3];
	int i,j;
	
	printf("Notas");
	for(j=0;j<1;j++)
	{
			for(i=0;i<2;i++)
		{
			printf("\t\t%i\t",i+1);
		}
		printf("\t\tMedia");
	}	
	printf("\n");
	printf("Alunos");
	printf("\n");

	for(i=0;i<20;i++)
	{
		printf("%i",i+1);
		for(j=0;j<3;j++)
		{
			colegio[i][j]=6;
			colegio[i][3]=colegio[i][1]+colegio[i][2];
			printf("\t\t%i\t",colegio[i][3]);
		}	
		printf("\n");
	}	
}
