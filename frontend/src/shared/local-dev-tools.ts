export type LocalDevTool = {
  name: string
  initials: string
  description: string
  href: string
}

export const localDevTools: LocalDevTool[] = [
  {
    name: "Keycloak",
    initials: "KC",
    description: "Identity provider",
    href: "http://localhost:9098",
  },
  {
    name: "Zipkin",
    initials: "ZP",
    description: "Distributed tracing",
    href: "http://localhost:9411",
  },
  {
    name: "MailDev",
    initials: "MD",
    description: "Local email inbox",
    href: "http://localhost:1080",
  },
  {
    name: "pgAdmin",
    initials: "PG",
    description: "Postgres administration",
    href: "http://localhost:5050",
  },
  {
    name: "Mongo Express",
    initials: "ME",
    description: "MongoDB administration",
    href: "http://localhost:8081",
  },
]
