main()
{
    int valor, contagem;
    int i=0;
    char resp;


    do
    {
        printf("Digite um valor: ");
        scanf("%i", &valor);
        getchar();

        if(valor>=0)
        {
            i++;
        }


        printf("Deseja sair? [S/N]");
        scanf("%c", &resp);

    }
    while(resp!='s');

    printf("%i", i);



}
