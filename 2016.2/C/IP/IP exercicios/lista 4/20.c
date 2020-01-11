#include<stdio.h>
#include<stdlib.h>

main()
{
	int m[5][5];
	int i,j,w=4,v=1;
	
	for(i=0;i<5;i++)
	{
		for(j=0;j<5;j++)
		{
			m[i][j]=0;
		}
	}	
	//Diagonal principal
	for(i=0;i<5;i++)
	{
		for(j=0;j<5;j++)
		{
			if(i==j)
			printf("%i\t",m[i][j]);
			else
			{
				printf("*\t");
			}
		}
		printf("\n");
	}
	system("pause");
	system("cls");
	//Tudo memos a diagonal princial
	for(i=0;i<5;i++)
	{	
		v++;
		for(i=0;i<=v;i++)
		printf("*");
		for(j=0;j<w;j++)
		{
			printf("%i",m[i][j+1]);
			
		}
		printf("\n");
		
		w--;
	}

}
