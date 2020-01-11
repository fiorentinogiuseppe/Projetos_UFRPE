#include<stdio.h>
#include<stdlib.h>

main()
{
	int c[2][3], c_2[3][2];
	int i,j;
	
	printf("C2x3");
	printf("\n\n");
	for(i=0;i<2;i++)
	{
		for(j=0;j<3;j++)
		{
			c[i][j]= 0;
			printf("%i\t", c[i][j]);
		}
		printf("\n");
	}
	system("pause");
	system("cls");
	printf("C3x2");

	printf("\n\n");
	for(i=0;i<3;i++)
	{
		for(j=0;j<2;j++)
		{
			c_2[i][j]= 0;
			printf("%i\t", c_2[i][j]);
		}
		printf("\n");
	}

}

