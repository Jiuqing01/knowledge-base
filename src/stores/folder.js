import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useFolderStore = defineStore('folder', () => {
  const currentFolder = ref(null)
  const folders = ref([])

  const setCurrentFolder = (folder) => {
    currentFolder.value = folder
  }

  const clearCurrentFolder = () => {
    currentFolder.value = null
  }

  const setFolders = (foldersList) => {
    folders.value = foldersList
  }

  const getFolderById = (folderId) => {
    return folders.value.find(f => f.id === folderId)
  }

  return {
    currentFolder,
    folders,
    setCurrentFolder,
    clearCurrentFolder,
    setFolders,
    getFolderById
  }
})