void swap(int* a, int* b) {
    int temp = *a;
    *a = *b;
    *b = temp;
}

int partition(int* arr, int low, int high) {
    int pivot = arr[high];
    int i = low - 1;
    for (int j = low; j <= high - 1; j++) {
        if (arr[j] < pivot) {
            i++;
            swap(&arr[i], &arr[j]);
        }
    }
    swap(&arr[i + 1], &arr[high]);
    return i + 1;
}

void quickSort(int* arr, int low, int high) {
    if (low < high) {
        int pi = partition(arr, low, high);
        quickSort(arr, low, pi - 1);
        quickSort(arr, pi + 1, high);
    }
}

int main() {
    int arr[6] = {10, 7, 8, 9, 1, 5};
    arr[0] = 10;
    arr[1] = 7;
    arr[2] = 8;
    arr[3] = 9;
    arr[4] = 1;
    arr[5] = 5;
    int n = 6;
    printf("原始数组: \n");
    for (int i = 0; i < n; i++)
    {
        printf("%d ", arr[i]);
    }

    quickSort(arr, 0, n - 1);

    printf("排序后的数组: \n");
    printArray(arr, n);
    return 0;
}