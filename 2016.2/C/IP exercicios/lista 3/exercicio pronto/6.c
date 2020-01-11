main()
{
    int m,li, ls,i,j;

    printf("Digite o Limite Inferior: ");
    scanf("%i", &li);
    printf("Digite o Limite Superior: ");
    scanf("%i", &ls);
    printf("Digite qual multiplo aparecerá na tela: ");
    scanf("%i", &m);
    system ("cls");

	for(i=0; i<ls;i++)
	{
		j=i*m;
		if(j>li && j<ls)
		{
			printf("%i\n",j);
		}
	}

}
