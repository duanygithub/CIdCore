int swap(int* a, int* b)
{
    int tmp = *a;
    *a = *b;
    *b = tmp;
    return *a + *b;
}

int main()
{
    int a = 999;
    int b = 666;
    int ret = 0;
    printf("a=%d b=%d ret=%d", a, b, ret);
}