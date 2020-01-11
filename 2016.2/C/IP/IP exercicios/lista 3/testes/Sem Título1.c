main()
{
    int i,j;

    for(i=0;i<=2;i++)
    {
        for(j=0;j<=2; j++)
        {
            if(j==i)
            {
                printf("%i-%i\t", i,j);
            }
            else
            {
                printf("\t");
            }
        }
        printf("\n");

    }
}

