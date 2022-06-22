package ru.itis.scalagram.notes

case class Notebook(id: Long, name: String, ownerId: Long)

case class Note(id: Long, title: String, notebookId: Long, content: String)
